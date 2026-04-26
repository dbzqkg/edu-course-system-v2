package com.lzh.result;

import com.lzh.enums.AppResultCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 全局统一响应包装类
 */
@Data
public class Result<T> implements Serializable {
    private Integer code;
    private String msg;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = AppResultCode.SUCCESS.getCode();
        result.msg = AppResultCode.SUCCESS.getMessage();
        result.data = data;
        return result;
    }

    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.code = code;
        result.msg = msg;
        return result;
    }

    public static <T> Result<T> error(AppResultCode resultCode) {
        return error(resultCode.getCode(), resultCode.getMessage());
    }
}