package com.lzh.scheduler;

import com.alibaba.fastjson2.JSON;
import com.lzh.entity.audit.SelectionLog;
import com.lzh.mapper.scheduling.SelectionLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j(topic = "REJECT_LOG_APPENDER")
@RequiredArgsConstructor
public class LogRecoveryScheduler implements InitializingBean {

    private final SelectionLogMapper selectionLogMapper;

    @Value("${app.logging.reject-path}")
    private String logPath;

    private static final int MAX_RETRY_COUNT = 3;
    private static final String IDENTIFIER = "TASK_REJECTED|";

    @Override
    public void afterPropertiesSet() throws Exception {
        if (logPath == null || logPath.isEmpty()) {
            log.error("致命错误：审计日志路径未配置，补偿机制失效！");
            return;
        }
        Path path = Paths.get(logPath);
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
    }

    /**
     * 核心调度：状态机互斥流
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void recoverFromDisk() {
        Path sourcePath = Paths.get(logPath);
        Path processingPath = Paths.get(logPath + ".processing");

        boolean hasOldJob = Files.exists(processingPath);
        boolean hasNewJob = Files.exists(sourcePath);

        if (!hasOldJob && !hasNewJob) return;

        if (hasOldJob) {
            log.warn("【自愈】检测到上次崩溃遗留的执行包，优先处理旧任务...");
        } else if (hasNewJob) {
            try {
                if (Files.size(sourcePath) > 0) {
                    // 使用 ATOMIC_MOVE 保证原子性，不带 REPLACE_EXISTING 保证安全
                    Files.move(sourcePath, processingPath, StandardCopyOption.ATOMIC_MOVE);
                } else {
                    return;
                }
            } catch (IOException e) {
                log.error("锁定新日志失败: {}", e.getMessage());
                return;
            }
        }

        // 进入真正的处理环节
        doProcess(processingPath);
    }

    /**
     * 【补全的核心方法】真正的文件处理逻辑
     * 逻辑：逐行读取 -> 反序列化 -> 幂等入库 -> 异常分流
     */
    private void doProcess(Path processingPath) {
        log.info("开始解析处理日志包: {}", processingPath.getFileName());

        List<String> retryLines = new ArrayList<>();
        List<String> fatalLines = new ArrayList<>();
        int successCount = 0;

        // 使用 try-with-resources 自动关闭流，防止文件句柄泄露
        try (BufferedReader reader = Files.newBufferedReader(processingPath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                // 1. 定位 JSON 内容
                int index = line.lastIndexOf(IDENTIFIER);
                if (index == -1) {
                    fatalLines.add(line);
                    continue;
                }

                String jsonContent = line.substring(index + IDENTIFIER.length());

                try {
                    // 2. Fastjson2 反序列化
                    SelectionLog selectionLog = JSON.parseObject(jsonContent, SelectionLog.class);

                    // 3. 执行入库
                    try {
                        selectionLogMapper.insert(selectionLog);
                        successCount++;
                    } catch (DuplicateKeyException e) {
                        // 幂等处理：主键冲突视为成功
                        log.debug("检测到重复记录（已由数据库拦截），跳过。ID: {}", selectionLog.getId());
                        successCount++;
                    } catch (Exception dbEx) {
                        // 数据库抖动，进入重试逻辑
                        handleRetryLogic(selectionLog, line, retryLines, fatalLines);
                    }
                } catch (Exception parseEx) {
                    log.error("JSON 解析失败，移入死信区: {}", line);
                    fatalLines.add(line);
                }
            }
        } catch (IOException e) {
            log.error("读取日志文件发生严重 I/O 异常: {}", e.getMessage());
            return; // 发生 I/O 错误时中断处理，防止误删文件
        }

        // 4. 收尾：写回重试数据、隔离致命数据、备份当前包
        finalizeBatch(processingPath, retryLines, fatalLines, successCount);
    }

    private void handleRetryLogic(SelectionLog entity, String originalLine, List<String> retryLines, List<String> fatalLines) {
        int currentRetry = entity.getRetryCount() == null ? 0 : entity.getRetryCount();
        if (currentRetry >= MAX_RETRY_COUNT) {
            log.error("数据重试 {} 次依然失败，移入致命隔离区: {}", MAX_RETRY_COUNT, originalLine);
            fatalLines.add(originalLine);
        } else {
            entity.setRetryCount(currentRetry + 1);
            retryLines.add(IDENTIFIER + JSON.toJSONString(entity));
        }
    }

    private void finalizeBatch(Path procPath, List<String> retryLines, List<String> fatalLines, int success) {
        try {
            // A. 将重试数据回填至原始 reject.log 底部
            if (!retryLines.isEmpty()) {
                Files.write(Paths.get(logPath), retryLines, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }

            // B. 致命错误隔离
            if (!fatalLines.isEmpty()) {
                Files.write(Paths.get(logPath + ".fatal"), fatalLines, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }

            // C. 归档本次处理的文件
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
            Files.move(procPath, Paths.get(logPath + "_" + time + ".bak"), StandardCopyOption.REPLACE_EXISTING);

            log.info("补偿包处理结束。成功: {}, 重试: {}, 致命错误: {}", success, retryLines.size(), fatalLines.size());
        } catch (IOException e) {
            log.error("补偿收尾阶段发生致命 I/O 错误: {}", e.getMessage());
        }
    }
}