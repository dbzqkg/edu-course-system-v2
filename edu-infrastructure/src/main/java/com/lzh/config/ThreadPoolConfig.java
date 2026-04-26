package com.lzh.config;

import com.lzh.handler.DiskLogRejectedExecutionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync // 开启 Spring 异步支持
public class ThreadPoolConfig {


    /**
     * 审计日志专用线程池
     * 针对日志这种“高吞吐、低优先级”的任务进行优化
     */
    @Bean("auditLogExecutor")
    public Executor auditLogExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程：通常设为 CPU 核心数
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        // 最大线程：选课瞬时并发极高，可以适当放大
        executor.setMaxPoolSize(20);
        // 缓冲队列：必须是有界队列，防止 OOM
        executor.setQueueCapacity(5000);
        executor.setThreadNamePrefix("Selection-Audit-");
        
        // 拒绝策略：如果日志队列满了，由调用方(主线程)执行，起到降级限流作用 （已废弃）
        // 磁盘记录加快处理时间
        executor.setRejectedExecutionHandler(new DiskLogRejectedExecutionHandler());
        
        executor.initialize();
        return executor;
    }
}