package com.zjzlj.seckilldemo.common.exception;


import com.zjzlj.seckilldemo.common.api.IErrorCode;

/**
 * @description: 自定义API异常
 * @create: 2020-06-27 09:03
 **/
public class ApiException extends RuntimeException{
    private IErrorCode errorCode;

    public ApiException(IErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ApiException(String message) {
        super(message);
    }

    public ApiException(Throwable cause) {
        super(cause);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public IErrorCode getErrorCode() {
        return errorCode;
    }
}
