package com.powernobug.mall.promo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.powernobug.mall.common.util.DateUtil;
import com.powernobug.mall.order.dto.OrderTradeDTO;
import com.powernobug.mall.order.query.OrderInfoParam;
import com.powernobug.mall.promo.api.dto.SeckillGoodsDTO;
import com.powernobug.mall.promo.constant.SeckillCodeEnum;
import com.powernobug.mall.promo.constant.SeckillGoodsStatus;
import com.powernobug.mall.promo.constant.SeckillGoodsStockStatus;
import com.powernobug.mall.promo.mapper.SeckillGoodsMapper;
import com.powernobug.mall.promo.model.SeckillGoods;
import com.powernobug.mall.promo.service.PromoService;
import com.powernobug.mall.promo.util.LocalCacheHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

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
    @Override
    public void importIntoRedis() {
        QueryWrapper<SeckillGoods> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status","CHECKED_PASS");
        queryWrapper.gt("stock_count",0);
        String currentDate = DateUtil.formatDate(new Date());
        queryWrapper.eq("DATE_FORMAT(start_time,'%Y-%m-%d')",currentDate);
        List<SeckillGoods> seckillGoods = seckillGoodsMapper.selectList(queryWrapper);

        for (SeckillGoods seckillGood : seckillGoods) {
            //为本地map设置值，有库存置为1
            Long skuId = seckillGood.getSkuId();
            LocalCacheHelper.put(skuId.toString(), SeckillGoodsStockStatus.HAS_STOCK);
        }


    }

    @Override
    public List<SeckillGoodsDTO> findAll() {
        return null;
    }

    @Override
    public SeckillGoodsDTO getSeckillGoodsDTO(Long skuId) {
        return null;
    }

    @Override
    public boolean checkOrder(Long skuId, String userId) {
        return false;
    }

    @Override
    public void clearRedisCache() {

    }

    @Override
    public OrderTradeDTO getTradeData(String userId, SeckillGoods seckillGoods) {
        return null;
    }

    @Override
    public boolean submitOrder(OrderInfoParam orderInfo) {
        return false;
    }

    @Override
    public void submitOrderInTransaction(OrderInfoParam orderInfo) {

    }
}
