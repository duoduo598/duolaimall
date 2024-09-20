package com.powernobug.mall.product.query;

import lombok.Data;

@Data
public class SkuSaleAttributeValueParam {

    // "id"
    private Long id;

    // "库存单元id"
    private Long skuId;

    // "spu_id"
    private Long spuId;

    // "销售属性值id"
    private Long saleAttrValueId;
}
