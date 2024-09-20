package com.cskaoyan.mall.product.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductTestDTO {

    String productName;

    BigDecimal price;

    String imgUrl;

}
