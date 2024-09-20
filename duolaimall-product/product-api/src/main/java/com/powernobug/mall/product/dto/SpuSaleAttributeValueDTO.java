package com.powernobug.mall.product.dto;

import lombok.Data;

@Data
public class SpuSaleAttributeValueDTO {

    //"id"
    private Long id;

    //"商品id"
    private Long spuId;

    //"销售属性id"
    private Long spuSaleAttrId;

    // "销售属性值名称"
    private String spuSaleAttrValueName;

    // "是否是当前sku商品的销售属性值"
    String isChecked;
}
