package com.lzh.entity.audit;

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
@TableName("edu_selection_log")
public class SelectionLog implements Serializable {
    @TableId(type = IdType.INPUT)
    private Long id;
    private Long studentId;
    private Long classId;
    private Integer operationType;
    private Integer selectionStatus;
    private String failReason;
    private String traceId;
    private Integer duration;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;
}