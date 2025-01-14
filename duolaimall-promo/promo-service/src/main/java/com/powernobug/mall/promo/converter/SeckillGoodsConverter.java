package com.powernobug.mall.promo.converter;

import com.powernobug.mall.order.dto.OrderDetailDTO;
import com.powernobug.mall.promo.api.dto.SeckillGoodsDTO;
import com.powernobug.mall.promo.model.SeckillGoods;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SeckillGoodsConverter {

    SeckillGoodsDTO convertSeckillGoodsToDTO(SeckillGoods seckillGoods);

    List<SeckillGoodsDTO> convertSeckillGoodsList(List<SeckillGoods> seckillGoodsList);

    SeckillGoods convertSeckillDTO(SeckillGoodsDTO seckillGoods);
    @Mapping(source = "seckillGoods.skuDefaultImg", target = "imgUrl")
    @Mapping(source = "seckillGoods.costPrice", target = "orderPrice")
    @Mapping(source = "num", target = "skuNum")
    OrderDetailDTO secondKillGoodsToOrderDetailDTO(SeckillGoods seckillGoods, Integer num);
}
