package com.powernobug.mall.cart.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;


@Data
public class CartInfoDTO {
    private static final long serialVersionUID = 1L;

    private String userId;

    private Long skuId;

    private BigDecimal cartPrice;

    private Integer skuNum;

    private String imgUrl;

    private String skuName;

    // 1： 选中  0：未选中
    private Integer isChecked = 1;

    // 实时价格 skuInfo.price
    BigDecimal skuPrice;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

}

