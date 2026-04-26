package com.lzh.audit;

import com.lzh.entity.audit.SelectionLog;
import com.lzh.context.StuContext;
import com.lzh.enums.SelectionSourceType; // 使用你刚创建的枚举
import com.lzh.util.SnowflakeIdUtil;
import com.lzh.util.ThreadLocalUtil;
import java.time.LocalDateTime;

/**
 * 选课审计工厂：支持全场景日志生产
 */
public class SelectionLogFactory {

    /**
     * 通用成功日志生产（支持抢课、预选、意向、置课）
     */
    public static SelectionLog createSuccess(Long classId, SelectionSourceType type, long duration, StuContext stu) {
        return buildLog(classId, type.getCode(), 1, "操作成功", duration, stu);
    }

    /**
     * 通用失败日志生产
     */
    public static SelectionLog createFail(Long classId, SelectionSourceType type, String error, long duration, StuContext stu) {
        return buildLog(classId, type.getCode(), 0, error, duration, stu);
    }

    /**
     * 核心生产逻辑：显式传入 StuContext，解决异步线程拿不到 ThreadLocal 的问题
     */
    static SelectionLog buildLog(Long classId, int sourceType, int status, String reason, long duration, StuContext stu) {
        // 健壮性检查：防止上下文丢失导致的 NPE
        Long studentId = (stu != null) ? stu.stuId() : 0L;

        return SelectionLog.builder()
                .id(SnowflakeIdUtil.getGeneratedId())
                .studentId(studentId)
                .classId(classId)
                .operationType(sourceType) // 这里的 1,2,3 对应不同的业务模式
                .selectionStatus(status)
                .failReason(reason)
                .duration((int) duration)
                .createBy(studentId)
                .createTime(LocalDateTime.now())
                .updateBy(studentId)
                .updateTime(LocalDateTime.now())
                .build();
    }
}