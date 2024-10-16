package com.powernobug.mall.mq.constant;

import lombok.Getter;

@Getter
public enum MqResultEnum{

    SEND_SUCCESS(50001, "发送成功"),
    SEND_FAIL(50002, "发送失败"),
    LOCAL_TRANSACTION_FAIL(50003, "本地事务执行失败"),
    LOCAL_TRANSACTION_EXCEPTION(50004, "本地事务执行异常"),
    Local_TRANSACTION_SUCCESS(50005, "本地事务执行成功");

    private Integer code;
    private String message;

    MqResultEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
