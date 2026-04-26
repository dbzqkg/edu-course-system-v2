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

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class SelectionLogAspect {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Around("@annotation(logAnnotation)")
    public Object doAround(ProceedingJoinPoint joinPoint, LogSelection logAnnotation) throws Throwable {
        long start = System.currentTimeMillis();
        String traceId = SnowflakeIdUtil.getGeneratedId() + "";
        StuContext stu = ThreadLocalUtil.get();
        Long classId = getTargetArgument(joinPoint, logAnnotation.key());

        // 【新增】从注解中动态解析枚举，避免硬编码
        SelectionSourceType sourceType = Arrays.stream(SelectionSourceType.values())
                .filter(s -> s.getCode() == logAnnotation.sourceType())
                .findFirst()
                .orElse(SelectionSourceType.ROBBING); // 默认为抢课

        Object result;
        try {
            result = joinPoint.proceed();

            // 【修正】传入动态解析的 sourceType
            SelectionLog logEntity = SelectionLogFactory.createSuccess(
                    classId, sourceType, System.currentTimeMillis() - start, stu
            );
            logEntity.setTraceId(traceId);
            eventPublisher.publishEvent(new SelectionLogEvent(logEntity));
            return result;
        } catch (Throwable e) {
            // 【修正】传入动态解析的 sourceType
            SelectionLog logEntity = SelectionLogFactory.createFail(
                    classId, sourceType, e.getMessage(), System.currentTimeMillis() - start, stu
            );
            logEntity.setTraceId(traceId);
            eventPublisher.publishEvent(new SelectionLogEvent(logEntity));
            throw e;
        }
    }

    private Long getTargetArgument(ProceedingJoinPoint joinPoint, String targetKey) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        if (parameterNames == null) return null;

        for (int i = 0; i < parameterNames.length; i++) {
            if (parameterNames[i].equals(targetKey)) {
                return (Long) args[i];
            }
        }
        return null;
    }
}