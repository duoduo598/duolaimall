package com.cskaoyan.mall.product.converter.dto;

import com.cskaoyan.mall.product.dto.ProductTestDTO;
import com.cskaoyan.mall.product.model.SkuInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TestProductConverter {

    // SkuInfo ——>  ProductTestDTO
    @Mapping(source = "skuName", target = "productName")
    @Mapping(source = "skuDefaultImg", target = "imgUrl")
   ProductTestDTO skuInfo2ProductTestDTO(SkuInfo skuInfo);

}
