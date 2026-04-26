package com.lzh.exception;

import com.lzh.enums.AppResultCode;
import lombok.Getter;

/**
 * 统一业务异常类
 * 1. 继承 RuntimeException 以支持 Spring 事务回滚
 * 2. 绑定 AppResultCode 枚举，规范化错误输出
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    /**
     * 根据预定义枚举创建异常
     */
    public BusinessException(AppResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    /**
     * 自定义错误信息的异常（用于覆盖枚举中的默认信息）
     */
    public BusinessException(AppResultCode resultCode, String customMessage) {
        super(customMessage);
        this.code = resultCode.getCode();
    }

    /**
     * 全能构造器
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}