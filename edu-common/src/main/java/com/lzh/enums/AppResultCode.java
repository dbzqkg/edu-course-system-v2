package com.lzh.enums;

public enum AppResultCode {
    // 2xx: 成功类
    SUCCESS(200, "操作成功"),

    // 4xx: 客户端/参数类错误
    PARAM_ERROR(4001, "请求参数非法"),
    UNAUTHORIZED(4002, "身份验证失败"),
    ACCESS_DENIED(4003, "权限不足"),

    // 1xx: 选课业务专属错误 (核心区分点)
    REPEAT_BOOK(1101, "您已经预选过该课程"),
    REQUIRED_COURSE_NO_BOOK(1102, "非可预选课程"),
    PHASE_NOT_OPEN(1103, "当前选课轮次未开启"),
    TIME_CONFLICT(1104, "课程时间冲突"),
    STOCK_EMPTY(1105, "课程名额已抢光"),

    // 5xx: 系统/中间件错误
    UNKNOWN(5000, "系统内部执行异常"),
    DB_WRITE_ERROR(5001, "系统繁忙，数据持久化失败"),
    REDIS_ERROR(5002, "缓存系统连接超时");

    private final Integer code;
    private final String message;

    AppResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}