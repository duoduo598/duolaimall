package com.powernobug.mall.common.handler;

import com.powernobug.mall.common.execption.BusinessException;
import com.powernobug.mall.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result error(Exception e){
        e.printStackTrace();
        return Result.fail();
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public Result error(BusinessException e){
        if (e.getCodeEnum() == null) {
            return Result.fail(e.getMessage());
        }
        return Result.build(null, e.getCodeEnum());
    }
}
