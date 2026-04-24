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
@TableName("edu_selection_intent")
public class SelectionIntent implements Serializable {
    @TableId(type = IdType.INPUT)
    private Long id;
    private Long studentId;
    private Long classId;
    private Integer priority; // 意向优先级 (1-5)
    private LocalDateTime createTime;
}