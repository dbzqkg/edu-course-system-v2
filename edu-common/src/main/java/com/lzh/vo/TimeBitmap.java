package com.lzh.vo;

import com.lzh.util.BitMapUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 时间位图值对象 - 解决类型冲突的关键
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeBitmap implements Serializable {
    private long[] bits;

    public boolean hasConflictWith(TimeBitmap other) {
        if (other == null || other.getBits() == null) return false;
        return BitMapUtil.isConflicting(this.bits, other.getBits());
    }

    /**
     * 选课动作：合并时间位图
     */
    public void addClass(TimeBitmap other) {
        if (other == null || other.getBits() == null) return;
        this.bits = BitMapUtil.merge(this.bits, other.getBits());
    }

    /**
     * 退课动作：扣除时间位图
     */
    public void dropClass(TimeBitmap other) {
        if (other == null || other.getBits() == null) return;

        // TODO: 在 Domain Service 层先判定该学生是否真的选了这门课。并

        this.bits = BitMapUtil.divide(this.bits, other.getBits());
    }
}