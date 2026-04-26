package com.lzh.aspect;

import com.lzh.annotations.LogSelection;
import com.lzh.audit.SelectionLogFactory;
import com.lzh.context.StuContext;
import com.lzh.entity.audit.SelectionLog;
import com.lzh.enums.SelectionSourceType;
import com.lzh.event.SelectionLogEvent;
import com.lzh.util.SnowflakeIdUtil;
import com.lzh.util.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class SelectionLogAspect {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Around("@annotation(logAnnotation)")
    public Object doAround(ProceedingJoinPoint joinPoint, LogSelection logAnnotation) throws Throwable {
        long start = System.currentTimeMillis();
        log.info("准备记录日志");

        // 1. 生成全链路追踪 ID
        String traceId = SnowflakeIdUtil.getGeneratedId() + "";

        // 2. 环境快照捕获：从 ThreadLocal 获取当前学生信息（此时还在主线程）
        StuContext stu = ThreadLocalUtil.get();

        // 3. 动态参数解析：根据注解指定的 key 获取 classId
        Long classId = getTargetArgument(joinPoint, logAnnotation.key());

        Object result;
        try {
            // 4. 执行核心业务（如 Redis 扣减、数据库写入）
            result = joinPoint.proceed();

            // 5. 成功审计：将 stu 快照传入工厂
            SelectionLog logEntity = SelectionLogFactory.createSuccess(
                    classId,
                    SelectionSourceType.ROBBING,
                    System.currentTimeMillis() - start,
                    stu
            );
            logEntity.setTraceId(traceId);

            // 发布异步事件，不阻塞主流程
            eventPublisher.publishEvent(new SelectionLogEvent(logEntity));

            return result;
        } catch (Throwable e) {
            // 6. 异常审计：记录失败原因并继续抛出异常
            SelectionLog logEntity = SelectionLogFactory.createFail(
                    classId,
                    SelectionSourceType.ROBBING,
                    e.getMessage(),
                    System.currentTimeMillis() - start,
                    stu
            );
            logEntity.setTraceId(traceId);

            eventPublisher.publishEvent(new SelectionLogEvent(logEntity));

            // 严禁吞掉异常，由 GlobalExceptionHandler 统一处理
            throw e;
        }
    }

    /**
     * 工业级参数解析：根据参数名获取值，避免 args[0] 的硬编码风险
     */
    private Long getTargetArgument(ProceedingJoinPoint joinPoint, String targetKey) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameterNames.length; i++) {
            if (parameterNames[i].equals(targetKey)) {
                return (Long) args[i];
            }
        }
        log.warn("AOP 警告：未找到指定的参数名 {}, 请检查 @LogSelection 注解配置", targetKey);
        return null;
    }
}