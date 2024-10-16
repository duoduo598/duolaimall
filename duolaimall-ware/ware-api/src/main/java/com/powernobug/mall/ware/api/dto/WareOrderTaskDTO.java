package com.powernobug.mall.ware.api.dto;

import lombok.Data;

import java.util.List;


@Data
public class WareOrderTaskDTO {

    private String orderId;

    private String consignee;

    private String consigneeTel;

    private String deliveryAddress;

    private String orderComment;

    private String paymentWay;

    private String taskStatus;

    private String orderBody;

    private String trackingNo;

    private String wareId;

    private String taskComment;

    private List<WareOrderTaskDetailDTO> details;
}
