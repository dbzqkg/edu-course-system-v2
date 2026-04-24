package com.lzh.audit;

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
@TableName("edu_admin_audit_log")
public class AdminAuditLog implements Serializable {
    @TableId(type = IdType.INPUT)
    private Long id;
    private Long operatorId;
    private String module;
    private String operation;
    private String businessId;
    private String oldValue;
    private String newValue;
    private String ipAddress;
    private LocalDateTime createTime;
}