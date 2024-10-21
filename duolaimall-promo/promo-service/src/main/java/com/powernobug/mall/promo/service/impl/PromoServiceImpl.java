package com.powernobug.mall.promo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.powernobug.mall.common.constant.RedisConst;
import com.powernobug.mall.common.execption.BusinessException;
import com.powernobug.mall.common.result.Result;
import com.powernobug.mall.common.util.DateUtil;
import com.powernobug.mall.mq.constant.MqResultEnum;
import com.powernobug.mall.mq.constant.MqTopicConst;
import com.powernobug.mall.order.dto.OrderDetailDTO;
import com.powernobug.mall.order.dto.OrderTradeDTO;
import com.powernobug.mall.order.query.OrderInfoParam;
import com.powernobug.mall.promo.api.dto.SeckillGoodsDTO;
import com.powernobug.mall.promo.client.OrderApiClient;
import com.powernobug.mall.promo.client.UserApiClient;
import com.powernobug.mall.promo.constant.SeckillCodeEnum;
import com.powernobug.mall.promo.constant.SeckillGoodsStatus;
import com.powernobug.mall.promo.constant.SeckillGoodsStockStatus;
import com.powernobug.mall.promo.converter.SeckillGoodsConverter;
import com.powernobug.mall.promo.mapper.SeckillGoodsMapper;
import com.powernobug.mall.promo.model.SeckillGoods;
import com.powernobug.mall.promo.mq.PromoTransactionProducer;
import com.powernobug.mall.promo.service.PromoService;
import com.powernobug.mall.promo.util.LocalCacheHelper;
import com.powernobug.mall.user.dto.UserAddressDTO;
import org.apache.commons.collections.CollectionUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.IntegerCodec;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @description:
 * @project: duolaimall
 * @package: com.powernobug.mall.promo.service.impl
 * @author: HuangWeiLong
 * @date: 2024/10/16 20:24
 */
@Service
public class PromoServiceImpl implements PromoService {
    @Autowired
    SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    SeckillGoodsConverter seckillGoodsConverter;
    @Autowired
    UserApiClient userApiClient;
    @Autowired
    OrderApiClient orderApiClient;
    @Autowired
    PromoTransactionProducer promoTransactionProducer;
    @Autowired
    RedissonClient redissonClient;
    @Override
    public void importIntoRedis() {
        List<SeckillGoods> seckillGoods = getCurrentDaySeckillGoods();

        for (SeckillGoods seckillGood : seckillGoods) {
            //为本地map设置值，有库存 置为1
            Long skuId = seckillGood.getSkuId();
            LocalCacheHelper.put(skuId.toString(), SeckillGoodsStockStatus.HAS_STOCK);
        }
        //将秒杀商品存入redis进行缓存预热
        RMap<Long, SeckillGoods> map1 = redissonClient.getMap(RedisConst.PROMO_SECKILL_GOODS);
        for (SeckillGoods seckillGood : seckillGoods) {
            Long skuId = seckillGood.getSkuId();
            map1.put(skuId,seckillGood);
        }
        //将库存数量也存入redis，活动结束在对数据库进行修改
        RMap<Long, Integer> map2 = redissonClient.getMap(RedisConst.PROMO_SECKILL_GOODS_STOCK, new StringCodec());
        for (SeckillGoods seckillGood : seckillGoods) {
            map2.put(seckillGood.getSkuId(),seckillGood.getStockCount());
        }
    }

    private List<SeckillGoods> getCurrentDaySeckillGoods() {
        QueryWrapper<SeckillGoods> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status","CHECKED_PASS");
        queryWrapper.gt("stock_count",0);
        String currentDate = DateUtil.formatDate(new Date());
        queryWrapper.eq("DATE_FORMAT(start_time,'%Y-%m-%d')",currentDate);
        List<SeckillGoods> seckillGoods = seckillGoodsMapper.selectList(queryWrapper);
        return seckillGoods;
    }

    @Override
    public List<SeckillGoodsDTO> findAll() {
        // 调用promoService的方法，根据如下条件查询
        // 1. 审核通过：status = CHECKED_PASS
        // 2. 今天开始：DATE_FORMAT(start_time,'%Y-%m-%d') = new Date()
        // 3. 库存大于0：stock_count>0
        RMap<Long, SeckillGoods> map = redissonClient.getMap(RedisConst.PROMO_SECKILL_GOODS);
        List<SeckillGoods> seckillGoods = map.readAllValues().stream().collect(Collectors.toList());

        RMap<Long, Integer> map1 = redissonClient.getMap(RedisConst.PROMO_SECKILL_GOODS_STOCK, IntegerCodec.INSTANCE);

        //其中的库存得从redis中去取
        for (SeckillGoods seckillGood : seckillGoods) {
            Integer stockCount = map1.get(seckillGood.getSkuId());
            seckillGood.setStockCount(stockCount);
        }
        return seckillGoodsConverter.convertSeckillGoodsList(seckillGoods);
    }

    @Override
    public SeckillGoodsDTO getSeckillGoodsDTO(Long skuId) {
        RMap<Long, SeckillGoods> map = redissonClient.getMap(RedisConst.PROMO_SECKILL_GOODS);
        List<SeckillGoods> seckillGoods = map.readAllValues().stream().collect(Collectors.toList());
        //当前的skuId的秒杀商品只能有一个
        List<SeckillGoods> filtedList = seckillGoods.stream().filter(seckillGood -> Objects.equals(seckillGood.getSkuId(), skuId)).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(filtedList)){
            return null;
        }
        SeckillGoods currentSeckillGoods = filtedList.get(0);
        return seckillGoodsConverter.convertSeckillGoodsToDTO(currentSeckillGoods);
    }

    @Override
    public boolean checkOrder(Long skuId, String userId) {
        String key=RedisConst.PROMO_SECKILL_ORDERS+userId+":"+skuId;
        RBucket<Long> bucket = redissonClient.getBucket(key);
        Long redisOrderId = bucket.get();
        if(redisOrderId!=null){
            return true;
        }
        return false;
    }

    @Override
    public void clearRedisCache() {
        //1.更新到数据库
        //2.状态设置为已结束
        RMap<Integer, Integer> map = redissonClient.getMap(RedisConst.PROMO_SECKILL_GOODS_STOCK,IntegerCodec.INSTANCE);
        for (Integer skuId : map.keySet()) {
            Integer stock = map.get(skuId);
            SeckillGoodsDTO seckillGoodsDTO = getSeckillGoodsDTO(Long.valueOf(skuId));
            SeckillGoods seckillGoods = seckillGoodsConverter.convertSeckillDTO(seckillGoodsDTO);
            seckillGoods.setStockCount(stock);
            seckillGoods.setStatus(SeckillGoodsStatus.FINISHED.name());
            int affectedRows = seckillGoodsMapper.updateById(seckillGoods);
            if(affectedRows<1){
             throw new RuntimeException("秒杀服务从redis缓存中更新库存失败！");
            }
        }
        //3.删除秒杀服务的redis缓存
        redissonClient.getKeys().deleteByPattern("promo:*");
        //4.删除本地库存状态位
        LocalCacheHelper.removeAll();
    }


    @Override
    public OrderTradeDTO getTradeData(String userId, Long skuId) {
        //封装useraddress对象
        List<UserAddressDTO> userAddressListByUserId = userApiClient.findUserAddressListByUserId(userId);
        //封装orderdetail对象
        SeckillGoodsDTO seckillGoodsDTO = getSeckillGoodsDTO(skuId);
        SeckillGoods seckillGoods = seckillGoodsConverter.convertSeckillDTO(seckillGoodsDTO);
        OrderDetailDTO orderDetailDTO = seckillGoodsConverter.secondKillGoodsToOrderDetailDTO(seckillGoods, 1);

        //封装各个参数
        OrderTradeDTO orderTradeDTO = new OrderTradeDTO();
        orderTradeDTO.setUserAddressList(userAddressListByUserId);
        orderTradeDTO.setDetailArrayList(Collections.singletonList(orderDetailDTO));
        orderTradeDTO.setTotalNum(1);
        orderTradeDTO.setTotalAmount(seckillGoods.getCostPrice());

        return orderTradeDTO;
    }

    /**
     * 提交秒杀订单
     *
     * @param
     * @return 补充说明：@Transactional事务失效的几种情况
     * 1. 在方法中，catch了异常，没有往外部抛异常
     * 2. @Transactional注解修饰的方法，不是public的
     * 3. 直接调用本地方法  this.xxx() , 假如xxx()方法有@Transactional注解，那么其实事务不会生效
     * 4. 数据库不支持事务（MySQL InnoDB是支持事务的， MyISAM是不支持事务的）
     * 5. 异常类型不匹配 (rollbackFor = xxx, 默认匹配的异常是一个RuntimeException运行时异常, 假如发生的不是运行时异常，那么事务就不会生效)
     * 6. Spring没有开启声明式事务的功能（@EnableTransactionManagement）
     * 7. 传播行为不支持事务 (Propagation.NOT_SUPPORTED | Propagation.NEVER)
     * <p>
     * 8. 在一个本地事务中，发生了本地数据库的操作，和远程调用的操作
     * 其实这种情况，本地的数据库事务就控制不了了
     * 为什么呢？因为本地事务生效的前提，是多个数据库操作基于同一个数据库连接
     * 只有多个数据库操作，是基于同一个数据库连接，才能让这多个数据库操作成为一个本地事务
     * <p>
     * 如果在一个方法中，有多步操作，这多步操作中，涉及到了服务间的远程调用，那么此时就产生了 【分布式事务】 的问题
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public boolean submitOrder(OrderInfoParam orderInfo) {
        //1.扣减库存
        Long skuId = orderInfo.getOrderDetailList().get(0).getSkuId();
        // 那么如何解决呢？ 本质上就是通过 锁 来解决
        // <a> 加分布式锁
        // <b> MySQL数据库默认情况下，会对每一次增删改操作加行锁
        //     补充：如果希望对查询语句也加行锁，那么需要在查询语句后面增加select * from user where id = 1  for update
        // 数据库的锁会随着SQL的执行结束而自动释放
        String currentDate = DateUtil.formatDate(new Date());
        int affectedRows=seckillGoodsMapper.decreaseStocks(skuId,1,currentDate);
        if(affectedRows<1){
            throw new RuntimeException("扣减库存异常！");
        }
        //2.生成订单
        Result ret = orderApiClient.submitOrder(orderInfo);
        return true;
    }

    @Override
    public void submitOrderInTransaction(OrderInfoParam orderInfo) {
        MqResultEnum resultEnum = promoTransactionProducer.sendMessageInTransaction(MqTopicConst.PROMO_ORDER_TOPIC, orderInfo);
        // 如果是本地事务执行成功
        if (MqResultEnum.Local_TRANSACTION_SUCCESS.equals(resultEnum)) {
            return;
        }

        // 如果是其他的情况 → 返回抢单失败
        // 本地事务执行失败 | 消息发送失败 | 本地事务执行异常
        throw new BusinessException(SeckillCodeEnum.SECKILL_FAIL);

    }
}
