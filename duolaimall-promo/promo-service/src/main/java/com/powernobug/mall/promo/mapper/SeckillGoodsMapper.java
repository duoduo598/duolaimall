package com.powernobug.mall.promo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.powernobug.mall.promo.model.SeckillGoods;
import org.apache.ibatis.annotations.Param;

public interface SeckillGoodsMapper extends BaseMapper<SeckillGoods> {

    int decreaseStocks(@Param("skuId") Long skuId, @Param("num") int num, @Param("currentDate") String currentDate);
}
