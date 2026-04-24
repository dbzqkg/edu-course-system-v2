package com.lzh.transaction;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

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
    private Integer sourceType; // 1:置课, 2:抢课成功, 3:意向中签
    private LocalDateTime createTime;
}