package com.zjzlj.seckilldemo.common.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonResult<T> {
    private long code;
    private String message;
    private T data;

    /**
    * @Description:  成功返回结果
    * @Param: [data]
    * @return: com.zjzlj.mall.common.api.CommonResult<T>
    * @Date: 
    */
    public static <T> CommonResult<T> success(T data) {
        return new CommonResult<T>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }
    
    /**
    * @Description:  成功返回结果
    * @Param: [data, message]
    * @return: com.zjzlj.mall.common.api.CommonResult<T>
    */
    public static <T> CommonResult<T> success(T data, String message) {
        return new CommonResult<T>(ResultCode.SUCCESS.getCode(), message, data);
    }

    /**
    * @Description:  失败返回结果
    * @Param: [resultCode]
    * @return: com.zjzlj.mall.common.api.CommonResult<T>
    */
    public static <T> CommonResult<T> failed(IErrorCode errorCode) {
        return new CommonResult<T>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    /**
    * @Description:失败返回结果
    * @Param: [errorCode, message]
    * @return: com.zjzlj.mall.common.api.CommonResult<T>
    */
    public static <T> CommonResult<T> failed(IErrorCode errorCode, String message) {
        return new CommonResult<T>(errorCode.getCode(), message, null);
    }
    
    /**
    * @Description: 失败返回结果
    * @Param: [message]
    * @return: com.zjzlj.mall.common.api.CommonResult<T>
    */
    public static <T> CommonResult<T> failed(String message) {
        return new CommonResult<T>(ResultCode.FAILED.getCode(), message, null);
    }

    /**
    * @Description: 失败返回结果
    * @Param: []
    * @return: com.zjzlj.mall.common.api.CommonResult<T>
    */
    public static <T> CommonResult<T> failed() {
        return failed(ResultCode.FAILED);
    }

    /**
    * @Description: 参数验证失败
    * @Param: []
    * @return: com.zjzlj.mall.common.api.CommonResult<T>
    */
    public static <T> CommonResult<T> validateFailed() {
        return failed(ResultCode.VALIDATE_FAILED);
    }

    /**
    * @Description: 参数验证失败
    * @Param: [message]
    * @return: com.zjzlj.mall.common.api.CommonResult<T>
    */
    public static <T> CommonResult<T> validateFailed(String message) {
        return new CommonResult<T>(ResultCode.VALIDATE_FAILED.getCode(), message, null);
    }

    /**
    * @Description: 未登录
    * @Param: [data]
    * @return: com.zjzlj.mall.common.api.CommonResult<T>
    */
    public static <T> CommonResult<T> unauthorized(T data) {
        return new CommonResult<T>(ResultCode.UNAUTHORIZED.getCode(), ResultCode.UNAUTHORIZED.getMessage(), data);
    }

    /**
    * @Description: 未授权
    * @Param: [data]
    * @return: com.zjzlj.mall.common.api.CommonResult<T>
    */
    public static <T> CommonResult<T> forbidden(T data) {
        return new CommonResult<T>(ResultCode.FORBIDDEN.getCode(), ResultCode.FORBIDDEN.getMessage(), data);
    }

    /**
     * @Description: 限流
     * @Param: [data]
     * @return: com.zjzlj.mall.common.api.CommonResult<T>
     */
    public static <T> CommonResult<T> rateLimit(T data) {
        return new CommonResult<T>(ResultCode.ACCESS_LIMIT.getCode(), ResultCode.ACCESS_LIMIT.getMessage(), data);
    }

    /**
     * @Description: 无库存
     * @Param: [data]
     * @return: com.zjzlj.mall.common.api.CommonResult<T>
     */
    public static <T> CommonResult<T> withoutStock(T data) {
        return new CommonResult<T>(ResultCode.WITHOUT_STOCK.getCode(), ResultCode.WITHOUT_STOCK.getMessage(), data);
    }


}
