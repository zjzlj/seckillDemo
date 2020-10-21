package com.zjzlj.seckilldemo.common.exception;


import com.zjzlj.seckilldemo.common.api.IErrorCode;

/**
 * @program: mall
 * @description: 断言处理类，用于抛出各种API异常
 * @create: 2020-06-27 09:09
 **/
public class Asserts {
    public static void Fail(String message) {
        throw new ApiException(message);
    }

    public static void Fail(IErrorCode errorCode) {
        throw new ApiException(errorCode);
    }
}
