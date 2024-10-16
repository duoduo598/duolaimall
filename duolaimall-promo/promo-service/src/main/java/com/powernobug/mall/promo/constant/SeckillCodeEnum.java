package com.powernobug.mall.promo.constant;

import com.powernobug.mall.common.constant.CodeEnum;

public enum SeckillCodeEnum implements CodeEnum {

    SECKILL_RUN(211, "正在排队中"),
    SECKILL_DUPLICATE_TRADE(212, "重复抢购商品"),

    //    SECKILL_NO_PAY_ORDER(212, "您有未支付的订单"),
    SECKILL_FINISH(213, "已售罄"),

    SECKILL_GET_USER_ADDRESS_FAIL(214, "获取用户地址列表失败"),
    //    SECKILL_END(214, "秒杀已结束"),
    SECKILL_SUCCESS(215, "抢单成功"),
    SECKILL_FAIL(216, "抢单失败"),
    SECKILL_ILLEGAL(217, "请求不合法"),
    SECKILL_ORDER_SUCCESS(218, "下单成功"),
    SECKILL_ORDER_TRY_AGAIN(219, "请稍后重试");
   ;


    private Integer code;

    private String message;

     SeckillCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
