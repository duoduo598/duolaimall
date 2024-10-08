package com.powernobug.mall.product.query;

import lombok.Data;

@Data
public class SkuPlatformAttributeValueParam {

    // "id"
    private Long id;

    // "属性id"
    private Long attrId;

    // "属性值id"
    private Long valueId;

    // "skuid"
    private Long skuId;
}
