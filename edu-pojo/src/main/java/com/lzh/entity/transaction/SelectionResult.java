package com.lzh.entity.transaction;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lzh.vo.TimeBitmap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 选课最终结果表 - 增加快照字段解决一致性问题
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("edu_selection_result")
public class SelectionResult implements Serializable {
    @TableId(type = IdType.INPUT)
    private Long id;

    private Long studentId;

    private Long classId;

    private Integer selectionStatus;

    /**
     * 来源 (1:置课, 2:抢课成功, 3:意向中签)
     */
    private Integer sourceType;

    /**
     * 【关键新增】选课瞬间的时间位图快照
     * 用于退课时精确回滚学生位图，防止因课程信息变更导致的退课逻辑错误
     */
    private TimeBitmap snapshotTimeBitmap;

}