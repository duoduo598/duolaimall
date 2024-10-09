package com.powernobug.mall.cart.converter;

import com.powernobug.mall.cart.api.dto.CartInfoDTO;
import com.powernobug.mall.product.dto.SkuInfoDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SkuInfoConverter {

    @Mapping(source = "skuId",target = "skuId")
    @Mapping(source = "skuInfo.price", target = "cartPrice")
    @Mapping(source = "skuNum", target = "skuNum")
    @Mapping(source = "skuInfo.skuDefaultImg", target = "imgUrl")
    @Mapping(source = "skuInfo.skuName",target = "skuName")
    @Mapping(target = "isChecked",constant = "1")
    @Mapping(source = "skuInfo.price", target = "skuPrice")
    @Mapping(target = "createTime", expression = "java( new java.util.Date())")
    @Mapping(target = "updateTime", expression = "java( new java.util.Date())")
    @Mapping(target = "userId",source = "userId")
    CartInfoDTO skuInfoToCartInfo(SkuInfoDTO skuInfo, Integer skuNum, Long skuId, String userId);

}
