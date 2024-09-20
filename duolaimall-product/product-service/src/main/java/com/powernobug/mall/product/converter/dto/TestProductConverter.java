package com.powernobug.mall.product.converter.dto;

import com.powernobug.mall.product.dto.ProductTestDTO;
import com.powernobug.mall.product.model.SkuInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TestProductConverter {

    // SkuInfo ——>  ProductTestDTO
    @Mapping(source = "skuName", target = "productName")
    @Mapping(source = "skuDefaultImg", target = "imgUrl")
    ProductTestDTO skuInfo2ProductTestDTO(SkuInfo skuInfo);

}
