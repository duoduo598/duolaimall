package com.powernobug.mall.product.dto;

import lombok.Data;

import java.util.List;

@Data
public class TrademarkPageDTO {

    // "查询到的一页品牌数据"
    private List<TrademarkDTO> records;
    // 总页数
    // "满足条件的总的品牌数量"
    private Integer total;
}
