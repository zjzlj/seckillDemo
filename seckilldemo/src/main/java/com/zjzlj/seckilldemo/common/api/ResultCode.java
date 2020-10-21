package com.zjzlj.seckilldemo.common.api;


import lombok.AllArgsConstructor;


@AllArgsConstructor
public enum ResultCode implements IErrorCode {
    SUCCESS(200, "操作成功"),
    FAILED(500, "操作失败"),
    VALIDATE_FAILED(404, "参数检验失败"),
    UNAUTHORIZED(401, "暂未登录或token已经过期"),
    ACCESS_LIMIT(402, "流量限制"),
    WITHOUT_STOCK(407, "无库存"),
    FORBIDDEN(403, "没有相关权限");



    Integer code;
    String message;

    @Override
    public long getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
