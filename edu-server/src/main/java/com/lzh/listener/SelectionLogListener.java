package com.lzh.listener;

import com.lzh.event.SelectionLogEvent;
import com.lzh.mapper.scheduling.SelectionLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 选课审计监听器：负责消费 SelectionLogEvent
 * 实现业务逻辑与日志落库的完全解耦
 */
@Component
@Slf4j
public class SelectionLogListener {

    @Autowired
    private SelectionLogMapper selectionLogMapper;

    /**
     * 核心处理方法
     * @Async 表示该方法会由独立线程池执行，不会阻塞主线程
     * @EventListener 告诉 Spring：只要有 SelectionLogEvent 发布，就传给这个方法
     */
    @Async("auditLogExecutor") 
    @EventListener
    public void onSelectionLogGenerated(SelectionLogEvent event) {
        try {
            // 从事件中取出之前 AOP 拼装好的日志实体
            var logEntity = event.getSelectionLog();
            
            log.debug("监听到选课事件，开始异步落库。TraceId: {}", logEntity.getTraceId());
            
            // 执行真正的数据库插入操作
            selectionLogMapper.insert(logEntity);
            
        } catch (Exception e) {
            // 工业级补救措施：异步链路的异常不会抛给前端，必须记录下来或进行二次重试
            log.error("异步写入审计日志失败，Payload: {}", event.getSelectionLog(), e);
        }
    }
}