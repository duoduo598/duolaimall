package com.powernobug.mall.ware.api.dto;

import lombok.Data;


@Data
public class WareOrderTaskDetailDTO {

    private String skuId;

    private String skuName;

    private Integer skuNum;

    private Long taskId;
}
