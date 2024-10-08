package com.powernobug.mall.product.query;

import lombok.Data;

@Data
public class SpuPosterParam {

    // "id"
    private Long id;

    // "商品id"
    private Long spuId;

    // "文件名称"
    private String imgName;

    // "文件路径"
    private String imgUrl;
}
