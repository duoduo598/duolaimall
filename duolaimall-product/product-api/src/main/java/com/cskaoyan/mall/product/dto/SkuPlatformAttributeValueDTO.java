package com.cskaoyan.mall.product.dto;

import lombok.Data;

@Data
public class SkuPlatformAttributeValueDTO {

    // "id"
    private Long id;

    // "属性id（冗余)"
    private Long attrId;

    // "属性值id"
    private Long valueId;

    // "skuid"
    private Long skuId;
}
