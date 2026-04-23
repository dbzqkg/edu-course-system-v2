package com.lzh.util;

public class BitMapUtils {
    /**
     * 判断两个位图是否冲突
     * @return true 表示有冲突（有重合位）, false 表示不冲突
     */
    public static boolean isConflicting(byte[] studentMap, byte[] classMap) {
        if (studentMap == null || classMap == null) return false;
        // 1024位对应128字节，逐字节进行按位与运算
        for (int i = 0; i < Math.min(studentMap.length, classMap.length); i++) {
            if ((studentMap[i] & classMap[i]) != 0) {
                return true; // 只要有一个字节的对应位同时为1，即冲突
            }
        }
        return false;
    }
}
