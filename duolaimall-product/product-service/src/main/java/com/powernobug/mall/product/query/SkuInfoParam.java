package com.powernobug.mall.product.query;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuInfoParam {

    //"id"
    private Long id;

    //"商品id"
    private Long spuId;

    // "价格"
    private BigDecimal price;

    // "sku名称"
    private String skuName;

    // "商品规格描述"
    private String skuDesc;

    //"重量"
    private String weight;

    // "品牌(冗余)"
    private Long tmId;

    // "三级分类id"
    private Long category3Id;

    // "默认显示图片"
    private String skuDefaultImg;

    //  "是否销售（1：是 0：否）"
    private Integer isSale;

    /*
         sku商品的图片列表
     */
    List<SkuImageParam> skuImageList;
    /*
         sku商品平台属性集合
     */
    List<SkuPlatformAttributeValueParam> skuAttrValueList;
    /*
         sku商品销售属性集合
     */
    List<SkuSaleAttributeValueParam> skuSaleAttrValueList;
}
