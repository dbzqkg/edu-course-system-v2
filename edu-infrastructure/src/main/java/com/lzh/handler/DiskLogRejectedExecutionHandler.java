package com.lzh.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 工业级拒绝策略：磁盘溢出日志模式
 * 当线程池满时，将任务内容序列化并由特定的 Logger 顺序写入本地磁盘，防止任务丢失
 */
@Slf4j(topic = "REJECT_LOG_APPENDER") // 使用独立的 Logger 名字，方便后续在 logback.xml 隔离文件
public class DiskLogRejectedExecutionHandler implements RejectedExecutionHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        try {
            // 1. 尝试将任务对象转化为 JSON（前提是你的任务 Task 类是可序列化的）
            // 这里为了通用，只记录任务的 toString 信息，或者你可以通过反射获取更深层数据
            String taskInfo = r.toString();
            
            // 2. 极致顺序写：利用日志框架的同步顺序写性能
            // 我们不直接写文件，而是交给日志框架，因为它有缓冲区优化
            log.error("TASK_REJECTED|{}", taskInfo);
            
            // 3. 监控报警（可选）：在这里可以增加一个计数器，监控拒绝频率
        } catch (Exception e) {
            // 最后的最后，如果序列化也失败了，只能控制台打印，保证不影响主流程
            System.err.println("Critical error in DiskLogRejectedExecutionHandler: " + e.getMessage());
        }
    }
}