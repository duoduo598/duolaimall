package com.powernobug.mall.promo.mq;

import com.alibaba.fastjson.JSON;
import com.powernobug.mall.common.constant.RedisConst;
import com.powernobug.mall.mq.constant.MqResultEnum;
import com.powernobug.mall.order.query.OrderInfoParam;
import com.powernobug.mall.promo.constant.SeckillGoodsStockStatus;
import com.powernobug.mall.promo.mapper.SeckillGoodsMapper;
import com.powernobug.mall.promo.redis.RedisStockOper;
import com.powernobug.mall.promo.util.LocalCacheHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.redisson.api.RBucket;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;

/**
 * 秒杀下单，事务消息的生产者
 */
@Slf4j
@Component
public class PromoTransactionProducer {

    // RocketMQ提供的事务消息的生产者
    TransactionMQProducer transactionMQProducer;

    @Autowired
    SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RedisStockOper redisStockOper;

    @PostConstruct
    public void init() {

        // 1. 创建对象
        transactionMQProducer = new TransactionMQProducer("promo-transaction-group");

        // 2. 设置注册中心
        transactionMQProducer.setNamesrvAddr("192.168.92.129:9876");

        // 3. 设置一个事务监听器  (对比普通消息生产者，多的部分)
        transactionMQProducer.setTransactionListener(new TransactionListener() {

            // 执行本地事务
            // 执行本地事务需要做什么事情呢？ 扣减库存

            @Override
            public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {

                // 消息id
                String transactionId = msg.getTransactionId();
                RBucket<String> bucket = redissonClient.getBucket(RedisConst.PROMO_TRANSACTION_PREFIX + transactionId);

                try {
                    log.info("本地事务开始执行...arg:{}", arg);
                    Long skuId = (Long) arg;

//                    String currentDate = DateUtil.formatDate(new Date());       // 获取到当前时间的年月日
//                    int affectedRows = seckillGoodsMapper.decreaseStock(skuId, 1, currentDate);
//                    if (affectedRows < 1) {
//                        // 更新本地库存状态位标记
//                        LocalCacheHelper.put(skuId.toString(), SeckillGoodsStockStatus.STOCK_NOT_ENOUGH.getCode());
//                        throw new RuntimeException("扣减库存失败");
//                    }

                    // 由于库存已经存入了Redis，那么在此处就可以直接去扣减Redis中的库存
                    // 等到活动结束，在统一把Redis中的库存更新到数据库
                    // 那么问题来了，如何扣减Redis中的库存呢？

                    // 第一种方案：
                        // 先查库存，然后减一，然后更新到Redis
                        // 这种做法有线程安全的问题，可能会导致 【少减、超卖】


                    // 第二种方案：
                        // 在第一种方案的基础之上加锁
                        // 加锁
                            //查询库存
                            //减一
                            // 更新到Redis
                        // 释放锁
                        // 这种做法可以保证线程安全，但是效率很低

                    // 第三种方案：
                        // 现在库存是使用Redis中的hash数据结构来存储的
                        // 每次是给库存 -1
                        // 那么可以考虑使用 hincrby这个命令来扣减库存
                        // 问题：hincrby减库存，会给库存减到负数,但是库存不能为负数


                    // 第四种方案：
                        // 在第三种方案的基础之上，加一个判断
                        //  先判断Redis中的库存是否够减
                            // 如果够减，那么使用hincrby去扣减库存
                            // 如果不够减，返回库存扣减失败
                        // 问题：判断库存和扣减库存不是一个原子操作,那么就可能出现以下的情况
                            // 线程A和线程B都来扣减库存
                            // 此时库存 = 1
                            // 线程A和线程B都判断有库存
                            // 线程A和线程B都去扣减库存
                            // 那么此时库存在被线程A和线程B扣减之后，库存又为负了
                            // 归根结底的原因，是因为判断操作和扣减库存的操作不是一个原子操作

                    // 那么如何让判断操作和扣减库存的操作成为一个原子操作呢？ 使用LUA脚本
                    // 因为Redis的底层是一个单线程工作模型，所以Redis执行一段LUA脚本，肯定是一个原子操作
                    // 那么我们只需要把 判断库存 和 扣减库存的操作都放到LUA脚本中来执行即可
                    Long remainStock = redisStockOper.decrRedisStock(skuId, 1);     // 返回的是剩余的库存数量

                    // 如果返回的库存 < 0 , 说明库存不足
                    if (remainStock < 0) {
                        bucket.set("fail");
                        return LocalTransactionState.ROLLBACK_MESSAGE;          // 返回本地事务执行失败
                    }

                    if (remainStock == 0) {

                        // 更新本地库存状态位
                        LocalCacheHelper.put(skuId.toString(), SeckillGoodsStockStatus.STOCK_NOT_ENOUGH.getCode());

                        bucket.set("success");
                        return LocalTransactionState.COMMIT_MESSAGE;                // 返回本地事务执行成功
                    }

                    log.info("本地事务执行结束...");
                }catch (Exception exception) {
                    exception.printStackTrace();
                    bucket.set("fail");
                    return LocalTransactionState.ROLLBACK_MESSAGE;          // 返回本地事务执行失败
                }
                bucket.set("success");
                return LocalTransactionState.COMMIT_MESSAGE;                // 返回本地事务执行成功
            }


            // 当RocketMQ没有收到本地事务的执行结果的时候，会进行回查 (RocketMQ每隔一分钟回查一次)
            // 检查本地事务的执行状态
            // 其实就是通过调用下面这个方法进行回查
            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt msg) {

                // 消息id
                String transactionId = msg.getTransactionId();
                RBucket<String> bucket = redissonClient.getBucket(RedisConst.PROMO_TRANSACTION_PREFIX + transactionId);

                log.info("RocketMQ的来回查本地事务的执行结果了...msg:{}", transactionId);


                // 演示本地事务的回查机制
                // return LocalTransactionState.UNKNOW;

                String flag = bucket.get();
                if (flag == null) {
                    // 暂时没有查到本地事务的执行结果
                    // 如果没有查到，那么RocketMQ会来重试，直到查到本地事务的执行结果为止
                    return LocalTransactionState.UNKNOW;
                }

                if (flag.equals("success")) {
                    // 说明本地事务执行成功了, 返回CommitMessage，那么就会去投递消息
                    return LocalTransactionState.COMMIT_MESSAGE;
                }

                // 返回本地事务执行失败，丢弃消息
                return LocalTransactionState.ROLLBACK_MESSAGE;
            }
        });

        // 4. 启动
        try {
            transactionMQProducer.start();
        } catch (MQClientException e) {
            throw new RuntimeException(e);
        }
        log.info("秒杀服务，事务消息生产者 启动了...");
    }

    /**
     * @param topic         消息的主题
     * @param messageObject 消息的内容
     * @return
     */
    public MqResultEnum sendMessageInTransaction(String topic, Object messageObject) {

        // 5. 创建消息
        String messageContent = JSON.toJSONString(messageObject);
        Message message = new Message(topic, messageContent.getBytes(StandardCharsets.UTF_8));

        // 6. 发送消息
        TransactionSendResult transactionSendResult = null;
        try {

            OrderInfoParam orderInfoParam = (OrderInfoParam)messageObject;
            Long skuId = orderInfoParam.getOrderDetailList().get(0).getSkuId();

            //获取事务消息的发送结果，以及本地事务的执行结果
            // 也就是意味着，当我们在此处获取到消息的发送结果的时候，本地事务已经执行了
            transactionSendResult = transactionMQProducer.sendMessageInTransaction(message, skuId);


            SendStatus sendStatus = transactionSendResult.getSendStatus();                                      // 获取消息的发送结果
            LocalTransactionState localTransactionState = transactionSendResult.getLocalTransactionState();     // 获取本地事务的执行状态

            // 发送事务消息失败了， 本地事务没有执行
            if (sendStatus == null || !SendStatus.SEND_OK.equals(sendStatus)) {

                // 针对消息发送失败的情况，其实本地事务没有执行（或者欢换句话来说，其实用户根本就没有参与抢购）
                // 我们后续希望用户能够再次来抢购
                // 需要把防止重复抢购的集合中的商品id删除掉，删除掉了之后，用户才能够再次来抢购
                String key = RedisConst.PROMO_ORDER_REPEAT + orderInfoParam.getUserId();
                RSet<Long> orderSet = redissonClient.getSet(key);
                orderSet.remove(skuId);

                return MqResultEnum.SEND_FAIL;          // 返回消息发送失败
            }

            // 如果代码走到这里，说明消息发送成功了

            // 说明消息发送成功，本地事务也执行成功
            if (LocalTransactionState.COMMIT_MESSAGE.equals(localTransactionState)) {
                return MqResultEnum.Local_TRANSACTION_SUCCESS;                              // 返回本地事务执行成功
            }

            // 说明消息发送成功，本地事务也执行失败了 → 那么此时就会把消息删除掉，不投递
            if (LocalTransactionState.ROLLBACK_MESSAGE.equals(localTransactionState)) {
                return MqResultEnum.LOCAL_TRANSACTION_FAIL;                                 // 返回本地事务执行失败
            }

            // 如果代码走到这里，说明消息发送成功，但是不知道本地事务的执行结果 (但是后续RocketMQ会自己来回查本地事务的执行结果)
        } catch (MQClientException e) {
            return MqResultEnum.LOCAL_TRANSACTION_EXCEPTION;
        }

        // 兜底
        return MqResultEnum.SEND_SUCCESS;
    }
}
