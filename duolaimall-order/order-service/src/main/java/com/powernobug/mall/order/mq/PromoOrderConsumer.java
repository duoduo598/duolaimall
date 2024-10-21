package com.powernobug.mall.order.mq;

import com.alibaba.fastjson2.JSON;
import com.powernobug.mall.common.constant.RedisConst;
import com.powernobug.mall.mq.constant.MqTopicConst;
import com.powernobug.mall.order.query.OrderInfoParam;
import com.powernobug.mall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.order.mq
 * @author: HuangWeiLong
 * @date: 2024/10/19 22:11
 */
@Slf4j
@Component
public class PromoOrderConsumer {
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    OrderService orderService;
    @PostConstruct
    public void init() throws MQClientException {
        //1.创建一个消息消费者对象
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("promo-transaction-consumer-group");

        //2.设置一个注册中心
        consumer.setNamesrvAddr("192.168.92.129:9876");

        //3.订阅一个主题
        consumer.subscribe(MqTopicConst.PROMO_ORDER_TOPIC,"*");


        //4.设置消息监听器
        consumer.setMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msg, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                MessageExt messageExt = msg.get(0);
                byte[] body = messageExt.getBody();
                String messageContent = new String(body);           // 获取消息内容
                log.info("秒杀订单消息消费者，收到了消息，消息内容是:" + messageContent);

                // 防止重复消费
                String msgId = messageExt.getMsgId();
                RBucket<Object> bucket = redissonClient.getBucket(RedisConst.PROMO_MESSAGE_REPEAT+msgId);
                boolean ret = bucket.trySet("ok");
                if(!ret){
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }

                try {
                    //如果orderId存在，那么消费成功，并且将orderId放入redis中，方便对是否生成订单进行检验
                    OrderInfoParam orderInfoParam = JSON.parseObject(messageContent, OrderInfoParam.class);
                    Long orderId = orderService.saveSeckillOrder(orderInfoParam);
                    if(orderId!=null){
                        Long userId = orderInfoParam.getUserId();
                        Long skuId = orderInfoParam.getOrderDetailList().get(0).getSkuId();
                        String key=RedisConst.PROMO_SECKILL_ORDERS+userId+":"+skuId;
                        RBucket<Long> bucket1 = redissonClient.getBucket(key);
                        bucket1.set(orderId);
                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // 删除幂等标记
                bucket.delete();
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
        });
        //5.启动
        consumer.start();
        log.info("秒杀订单，消息消费者实现了...");
    }

}
