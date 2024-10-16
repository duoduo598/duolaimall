package com.powernobug.mall.promo.service;

import com.powernobug.mall.order.dto.OrderTradeDTO;
import com.powernobug.mall.order.query.OrderInfoParam;
import com.powernobug.mall.promo.api.dto.SeckillGoodsDTO;
import com.powernobug.mall.promo.model.SeckillGoods;

import java.util.List;

public interface PromoService {

    /**
     * 把秒杀商品列表信息导入Redis
     */
    void importIntoRedis();

    /**
     * 返回全部列表
     * @return
     */
    List<SeckillGoodsDTO> findAll();

    /**
     * 根据ID获取实体
     * @param skuId
     * @return
     */
    SeckillGoodsDTO getSeckillGoodsDTO(Long skuId);


    /***
     * 根据商品id与用户ID查看订单信息
     */
    boolean checkOrder(Long skuId, String userId);

    /**
     * 清理Redis缓存
     */
    void clearRedisCache();

    /*
         组装订单确认页数据
     */
    OrderTradeDTO getTradeData(String userId, SeckillGoods seckillGoods);

    /*
         提交秒杀订单
     */
    boolean submitOrder(OrderInfoParam orderInfo);

    void submitOrderInTransaction(OrderInfoParam orderInfo);

}
