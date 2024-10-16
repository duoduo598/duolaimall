package com.powernobug.mall.order.dto;

import com.powernobug.mall.order.constant.OrderType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 创建日期: 2023/03/16 17:25
 *
 * @author ciggar
 */
@Data
public class OrderInfoDTO {

    private Long id;


    private Long parentOrderId;

    private String orderStatus;

    private Long userId;

    private String paymentWay;

    private String consignee;

    private String consigneeTel;

    private String deliveryAddress;

    private BigDecimal totalAmount;

    private BigDecimal originalTotalAmount;

    private String orderComment;

    private String outTradeNo;

    private String tradeBody;

    private String orderType = OrderType.NORMAL_ORDER.name();


    private String trackingNo;

    private Date refundableTime;


    private Date createTime;

    private Date updateTime;

    private Date expireTime;


    private List<OrderDetailDTO> orderDetailList;


    private String wareId;

    private String orderStatusName;

    // 计算总价格
    public void sumTotalAmount(){
        BigDecimal totalAmount = new BigDecimal("0");
        //  计算最后
        for (OrderDetailDTO orderDetail : orderDetailList) {
            BigDecimal skuTotalAmount = orderDetail.getOrderPrice().multiply(new BigDecimal(orderDetail.getSkuNum()));
            totalAmount = totalAmount.add(skuTotalAmount);
        }
        this.setTotalAmount(totalAmount);
        this.setOriginalTotalAmount(totalAmount);
    }

}
