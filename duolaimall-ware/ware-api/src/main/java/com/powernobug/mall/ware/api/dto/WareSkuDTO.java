package com.powernobug.mall.ware.api.dto;

import lombok.Data;

import java.util.List;


@Data
public class WareSkuDTO {

    // 仓库id
    String wareId;

    // 商品id列表
    List<String> skuIds;

}
