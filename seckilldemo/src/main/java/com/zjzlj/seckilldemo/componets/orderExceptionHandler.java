package com.zjzlj.seckilldemo.componets;

import com.zjzlj.seckilldemo.common.api.CommonResult;
import com.zjzlj.seckilldemo.common.exception.ApiException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class orderExceptionHandler {

    @ResponseBody
    @ExceptionHandler(ApiException.class)
    public CommonResult handle(ApiException e) {
        if(e.getErrorCode()!=null){
            return CommonResult.failed(e.getErrorCode());
        }
        return CommonResult.failed(e.getMessage());
    }
}
