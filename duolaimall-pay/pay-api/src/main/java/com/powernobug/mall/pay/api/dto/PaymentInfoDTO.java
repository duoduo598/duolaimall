package com.powernobug.mall.pay.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 创建日期: 2023/03/17 16:55
 *
 * @author ciggar
 */
@Data
public class PaymentInfoDTO {

    Long id;

    // 对外业务编号
    private String outTradeNo;

    // 订单编号
    private Long orderId;

    // 用户Id
    private Long userId;

    // 支付类型（微信 支付宝）
    private String paymentType;

    // 交易编号
    private String tradeNo;

    // 支付金额
    private BigDecimal totalAmount;

    // 交易内容
    private String subject;

    // 支付状态
    private String paymentStatus;

    // 创建时间
    private Date createTime;

    // 回调时间
    private Date callbackTime;

    // 回调信息
    private String callbackContent;
}
