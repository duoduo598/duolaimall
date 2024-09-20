package com.cskaoyan.mall.product.dto;

import lombok.Data;

@Data
public class SpuPosterDTO {
    //"id"
    private Long id;

    // "商品id"
    private Long spuId;

    // "文件名称"
    private String imgName;

    // "文件路径"
    private String imgUrl;
}
