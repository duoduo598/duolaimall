package com.powernobug.mall.product.dto;

import lombok.Data;

@Data
public class SkuSaleAttributeValueDTO {

    //"id"
    private Long id;

    //"库存单元id"
    private Long skuId;

    // "spu_id(冗余)"
    private Long spuId;

    // "销售属性值id"
    private Long saleAttrValueId;
}
