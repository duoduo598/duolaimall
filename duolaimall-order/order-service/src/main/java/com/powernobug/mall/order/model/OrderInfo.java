package com.powernobug.mall.order.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.powernobug.mall.common.model.BaseEntity;
import com.powernobug.mall.order.constant.OrderType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@TableName("order_info")
public class OrderInfo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableField("parent_order_id")
    private Long parentOrderId;

    @TableField("order_status")
    private String orderStatus;

    @TableField("user_id")
    private Long userId;

    @TableField("payment_way")
    private String paymentWay = "ONLINE";

    @TableField("consignee")
    private String consignee;

    @TableField("consignee_tel")
    private String consigneeTel;

    @TableField("delivery_address")
    private String deliveryAddress;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    @TableField("original_total_amount")
    private BigDecimal originalTotalAmount;

    @TableField("order_comment")
    private String orderComment;

    @TableField("out_trade_no")
    private String outTradeNo;

    @TableField("trade_body")
    private String tradeBody;

    @TableField("order_type")
    private String orderType = OrderType.NORMAL_ORDER.name();


    @TableField("tracking_no")
    private String trackingNo;

    // 可退款日期
    @TableField("refundable_time")
    @JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
    private Date refundableTime;


    @TableField("expire_time")
    @JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
    private Date expireTime;


    @TableField(exist = false)
    private List<OrderDetail> orderDetailList;

    @TableField(exist = false)
    private String wareId;

    // 计算总价格
    public void sumTotalAmount(){
        BigDecimal totalAmount = new BigDecimal("0");
        //  计算最后
        for (OrderDetail orderDetail : orderDetailList) {
            BigDecimal skuTotalAmount = orderDetail.getOrderPrice().multiply(new BigDecimal(orderDetail.getSkuNum()));
            totalAmount = totalAmount.add(skuTotalAmount);
        }
        this.setTotalAmount(totalAmount);
        this.setOriginalTotalAmount(totalAmount);
    }
}
