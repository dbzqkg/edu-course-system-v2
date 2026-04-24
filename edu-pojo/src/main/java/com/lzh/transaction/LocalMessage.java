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
@TableName("edu_local_message")
public class LocalMessage implements Serializable {
    @TableId(type = IdType.INPUT)
    private Long id;
    private String businessId;
    private String payload;
    private Integer status; // 0:处理中, 1:已完成, 2:失败
    private Integer retryCount;
    private LocalDateTime updateTime;
}