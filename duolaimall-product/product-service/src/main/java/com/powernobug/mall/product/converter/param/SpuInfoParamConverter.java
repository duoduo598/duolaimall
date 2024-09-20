package com.powernobug.mall.product.converter.param;

import com.powernobug.mall.product.model.*;
import com.powernobug.mall.product.query.SpuImageParam;
import com.powernobug.mall.product.query.SpuInfoParam;
import com.powernobug.mall.product.query.SpuSaleAttributeInfoParam;
import com.powernobug.mall.product.query.SpuSaleAttributeValueParam;
import com.powernobug.mall.product.model.SpuImage;
import com.powernobug.mall.product.model.SpuInfo;
import com.powernobug.mall.product.model.SpuSaleAttributeInfo;
import com.powernobug.mall.product.model.SpuSaleAttributeValue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SpuInfoParamConverter {

    @Mapping(source = "category3Id", target = "thirdLevelCategoryId")
    @Mapping(source = "spuSaleAttrList", target = "spuSaleAttributeInfoList")
    SpuInfo spuInfoParam2Info(SpuInfoParam spuInfo);

    SpuImage spuImageParam2Image(SpuImageParam spuImage);
    List<SpuImage> spuImageParams2Images(List<SpuImageParam> spuImages);

    @Mapping(source = "baseSaleAttrId", target = "saleAttrId")
    SpuSaleAttributeInfo spuSaleAttributeParam2Info(SpuSaleAttributeInfoParam spuSaleAttributeInfo);
    List<SpuSaleAttributeInfo> spuSaleAttributeParams2Infos(List<SpuSaleAttributeInfoParam> spuSaleAttributeInfos);
    @Mapping(source = "saleAttrValueName", target = "spuSaleAttrValueName")
    @Mapping(source = "baseSaleAttrId", target = "spuSaleAttrId")
    SpuSaleAttributeValue spuSaleAttributeValueParam2Value (SpuSaleAttributeValueParam spuSaleAttributeValue);
    List<SpuSaleAttributeValue> spuSaleAttributeValueParams2Values (List<SpuSaleAttributeValueParam> spuSaleAttributeValues);
}
