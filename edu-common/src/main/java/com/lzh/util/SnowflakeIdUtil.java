package com.lzh.util;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 改进版雪花算法工具类
 * 1. 增加时钟回拨处理：小跨度回拨自动等待，大跨度回拨报错
 * 2. 序列号起始随机：避免在低并发下产生的 ID 末尾总是 0
 * 3. 线程安全：基于 synchronized 锁定实例
 */
public class
SnowflakeIdUtil {

    // ============================== 基础配置 ==============================
    // 起始时间戳 (2024-01-01 00:00:00)
    private final long twepoch = 1704067200000L;

    // 机器标识占用的位数
    private final long workerIdBits = 5L;
    private final long datacenterIdBits = 5L;
    // 序列号占用的位数
    private final long sequenceBits = 12L;

    // 偏移量计算
    private final long workerIdShift = sequenceBits;
    private final long datacenterIdShift = sequenceBits + workerIdBits;
    private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

    // 掩码
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);
    private final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    // 状态变量
    private long workerId;
    private long datacenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    // ============================== 改进点：时钟回拨阈值 ==============================
    private static final long MAX_BACKWARDS_MS = 5; // 允许最大回拨 5 毫秒

    private SnowflakeIdUtil(long workerId, long datacenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    /**
     * 核心方法：获取下一个 ID
     */
    public synchronized long nextId() {
        long timestamp = timeGen();

        // 改进 1：处理时钟回拨
        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            if (offset <= MAX_BACKWARDS_MS) {
                // 如果回拨时间较短，强制等待并重新获取时间
                try {
                    wait(offset << 1);
                    timestamp = timeGen();
                    if (timestamp < lastTimestamp) {
                        throw new RuntimeException("时钟回拨后等待失败");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            } else {
                throw new RuntimeException("时钟回拨跨度过大，拒绝生成 ID");
            }
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 改进 2：不同毫秒内，序列号从随机值开始 (0~9)，避免 ID 规律性太强
            sequence = ThreadLocalRandom.current().nextLong(10);
        }

        lastTimestamp = timestamp;

        return ((timestamp - twepoch) << timestampLeftShift)
                | (datacenterId << datacenterIdShift)
                | (workerId << workerIdShift)
                | sequence;
    }

    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    protected long timeGen() {
        return System.currentTimeMillis();
    }

    // ============================== 单例持有者 ==============================
    private static class Holder {
        // 实际开发中，workerId 和 datacenterId 建议从环境变量或配置文件读取
        private static final SnowflakeIdUtil INSTANCE = new SnowflakeIdUtil(1, 1);
    }

    public static long getGeneratedId() {
        return Holder.INSTANCE.nextId();
    }
}
