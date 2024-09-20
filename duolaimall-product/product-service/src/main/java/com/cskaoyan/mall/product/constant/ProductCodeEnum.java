package com.cskaoyan.mall.product.constant;

import com.cskaoyan.mall.common.constant.CodeEnum;
import lombok.Getter;

@Getter
public enum ProductCodeEnum implements CodeEnum {

    UPLOAD_FILE_FAILED(221, "文件上传失败");

    ProductCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    private Integer code;

    private String message;
}
