package com.lzh.handler;

import com.lzh.exception.BusinessException;
import com.lzh.result.Result;
import com.lzh.enums.AppResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 工业级全局异常拦截器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 捕获自定义业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("业务逻辑异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 捕获系统未知异常（如 SQL 语法错误、空指针等）
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        // 生产环境下，未知异常必须打出堆栈，方便排查
        log.error("系统运行异常: ", e);
        return Result.error(AppResultCode.UNKNOWN.getCode(), AppResultCode.UNKNOWN.getMessage());
    }
}