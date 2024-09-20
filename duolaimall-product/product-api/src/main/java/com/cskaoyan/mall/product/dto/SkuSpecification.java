package com.cskaoyan.mall.product.dto;

import lombok.Data;

/*
     SkuSpecification 用来描述商品详情页中，商品的规格参数，
     一个SkuSpecification对象，封装一个规格参数的参数名和参数值
     比如，运行内存: 8G
 */
@Data
public class SkuSpecification {

    //"规格参数名称"
    private String attrName;
    //"规格参数值"
    private String attrValue;
}
