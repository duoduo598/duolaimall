package com.powernobug.mall.product.converter.param;

import com.powernobug.mall.product.model.PlatformAttributeInfo;
import com.powernobug.mall.product.model.PlatformAttributeValue;
import com.powernobug.mall.product.query.PlatformAttributeParam;
import com.powernobug.mall.product.query.PlatformAttributeValueParam;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlatformAttributeInfoParamConverter {

    PlatformAttributeInfo attributeInfoParam2Info(PlatformAttributeParam platformAttributeParam);

    PlatformAttributeValue attributeValueParam2AttributeValue(PlatformAttributeValueParam platformAttributeValueParam);

}
