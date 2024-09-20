package com.powernobug.mall.product.converter.dto;

import com.powernobug.mall.product.dto.SkuImageDTO;
import com.powernobug.mall.product.dto.SkuInfoDTO;
import com.powernobug.mall.product.dto.SkuPlatformAttributeValueDTO;
import com.powernobug.mall.product.dto.SkuSaleAttributeValueDTO;
import com.powernobug.mall.product.model.SkuImage;
import com.powernobug.mall.product.model.SkuInfo;
import com.powernobug.mall.product.model.SkuPlatformAttributeValue;
import com.powernobug.mall.product.model.SkuSaleAttributeValue;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SkuInfoConverter {

    SkuInfoDTO skuInfoPO2DTO(SkuInfo skuInfo);

    SkuImageDTO skuImagePO2DTO(SkuImage skuImage);

    SkuPlatformAttributeValueDTO skuPlatformAttributeValuePO2DTO(
            SkuPlatformAttributeValue skuPlatformAttributeValue);

    SkuSaleAttributeValueDTO skuSaleAttributeValuePOs2DTOs(
            SkuSaleAttributeValue skuSaleAttributeValue);
}
