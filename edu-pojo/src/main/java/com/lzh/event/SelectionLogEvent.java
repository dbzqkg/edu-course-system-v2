package com.lzh.event;


import com.lzh.entity.audit.SelectionLog;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 工业级规范：POJO 事件模型
 * 无需继承 ApplicationEvent，保持模型层的零技术污染
 */
@Getter
@RequiredArgsConstructor
public class SelectionLogEvent {
    // 携带的日志实体
    private final SelectionLog selectionLog;
}