package com.cskaoyan.mall.common.constant;

import lombok.Getter;

/**
 * 统一返回结果状态信息类
 *
 */
@Getter
public enum ResultCodeEnum implements CodeEnum {

    SUCCESS(200,"成功"),
    FAIL(201, "失败"),
    SERVICE_ERROR(500, "服务异常"),
    ILLEGAL_REQUEST( 204, "非法请求"),
//    PAY_RUN("205", "支付中"),
//
    LOGIN_AUTH(208, "未登陆"),
    PERMISSION(209, "没有权限");
//    SECKILL_NO_START(210, "秒杀还没开始"),


    private Integer code;

    private String message;

    private ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
