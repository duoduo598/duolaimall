package com.powernobug.mall.product.converter.param;

import com.powernobug.mall.product.model.SkuImage;
import com.powernobug.mall.product.model.SkuInfo;
import com.powernobug.mall.product.model.SkuPlatformAttributeValue;
import com.powernobug.mall.product.model.SkuSaleAttributeValue;
import com.powernobug.mall.product.query.SkuImageParam;
import com.powernobug.mall.product.query.SkuInfoParam;
import com.powernobug.mall.product.query.SkuPlatformAttributeValueParam;
import com.powernobug.mall.product.query.SkuSaleAttributeValueParam;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SkuInfoParamConverter {

    @Mapping(source = "category3Id", target = "thirdLevelCategoryId")
    @Mapping(source = "skuAttrValueList", target = "skuPlatformAttributeValueList")
    @Mapping(source = "skuSaleAttrValueList", target = "skuSaleAttributeValueList")
    SkuInfo SkuInfoParam2Info(SkuInfoParam skuInfoParam);

    SkuImage skuImageParam2Image(SkuImageParam skuImageParam);

    SkuPlatformAttributeValue skuPlatformAttributeValueParam2Value(SkuPlatformAttributeValueParam param);

    @Mapping(source = "saleAttrValueId", target = "spuSaleAttrValueId")
    SkuSaleAttributeValue skuSaleAttributeValueParam2Value(SkuSaleAttributeValueParam param);

}
