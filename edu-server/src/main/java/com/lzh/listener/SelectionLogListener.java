package com.lzh.listener;

import com.lzh.event.SelectionLogEvent;
import com.lzh.mapper.scheduling.SelectionLogMapper;
import com.lzh.task.SelectionLogTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

/**
 * 选课审计监听器：负责消费 SelectionLogEvent
 * 实现业务逻辑与日志落库的完全解耦
 */
@Component
@Slf4j
@RequiredArgsConstructor // Lombok 会自动生成包含所有 final 字段的构造器
public class SelectionLogListener {

    private final SelectionLogMapper selectionLogMapper;

    // 1. 使用 final 保证不可变性
    // 2. 配合 Qualifier 解决多 Bean 注入歧义
    @Qualifier("auditLogExecutor")
    private final Executor auditLogExecutor;

    @EventListener
    public void onSelectionLogGenerated(SelectionLogEvent event) {
        // 关键逻辑：手动提交显式任务，确保拒绝策略可追溯
        var logEntity = event.getSelectionLog();

        log.debug("监听到选课日志事件，准备提交异步任务。TraceId: {}", logEntity.getTraceId());

        // 提交自定义任务，SelectionLogTask 必须重写了 toString()
        auditLogExecutor.execute(new SelectionLogTask(logEntity, selectionLogMapper));
    }
}