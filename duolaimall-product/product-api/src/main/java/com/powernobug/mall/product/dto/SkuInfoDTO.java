package com.powernobug.mall.product.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
@Data
public class SkuInfoDTO {
    //"id"
    private Long id;

    // "商品id"
    private Long spuId;

    // "价格"
    private BigDecimal price;

    // "sku名称"
    private String skuName;

    // "商品规格描述"
    private String skuDesc;

    // "重量"
    private String weight;

    // "品牌(冗余)"
    private Long tmId;

    // "三级分类id（冗余)"
    private Long thirdLevelCategoryId;

    // "默认显示图片(冗余)"
    private String skuDefaultImg;

    // "是否销售（1：是 0：否）"
    private Integer isSale;

    // "sku商品图片列表"
    List<SkuImageDTO> skuImageList;
    // "sku商品平台属性值集合"
    List<SkuPlatformAttributeValueDTO> skuPlatformAttributeValueList;
    // "sku商品销售属性值集合"
    List<SkuSaleAttributeValueDTO> skuSaleAttributeValueList;

}
