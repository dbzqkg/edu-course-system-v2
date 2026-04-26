package com.lzh.annotations;

import java.lang.annotation.*;

/**
 * 选课审计日志注解 - 贴在 Controller 方法上即刻生效
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogSelection {
    /**
     * 操作描述，例如 "抢选体育课"
     */
    String value() default "选课操作";

    /**
     * 业务类型标识 (1:置课, 2:抢课, 3:意向)
     */
    int sourceType() default 2;

    // 新增：用于指定方法参数中哪个是 classId 的名称
    String key() default "classId";
}