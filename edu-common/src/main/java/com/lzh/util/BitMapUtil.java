package com.lzh.util;

public class BitMapUtil {

    // 西电标准：1024位 = 16个long
    private static final int BITMAP_SIZE = 16;

    public static boolean isConflicting(long[] map1, long[] map2) {
        if (map1 == null || map2 == null) return false;
        // 生产级：直接循环固定长度，性能最稳
        int len = Math.min(map1.length, map2.length);
        for (int i = 0; i < len; i++) {
            if ((map1[i] & map2[i]) != 0) return true;
        }
        return false;
    }

    public static long[] merge(long[] source, long[] target) {
        if (source == null) return target;
        if (target == null) return source;

        long[] result = new long[BITMAP_SIZE];
        for (int i = 0; i < BITMAP_SIZE; i++) {
            long s = i < source.length ? source[i] : 0L;
            long t = i < target.length ? target[i] : 0L;
            result[i] = s | t; // 按位或：合并时间片
        }
        return result;
    }

    /**
     * 扣除位图：从 source 中移除 target 的时间片
     */
    public static long[] divide(long[] source, long[] target) {
        if (source == null) return null; // 课表为空，移除后依然为空
        if (target == null) return source; // 移除目标为空，返回原课表

        long[] result = new long[BITMAP_SIZE];
        for (int i = 0; i < BITMAP_SIZE; i++) {
            long s = i < source.length ? source[i] : 0L;
            long t = i < target.length ? target[i] : 0L;
            result[i] = s & (~t);
        }
        return result;
    }
}

