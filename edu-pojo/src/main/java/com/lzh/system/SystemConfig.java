package com.lzh.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("edu_system_config")
public class SystemConfig implements Serializable {
    @TableId(type = IdType.INPUT)
    private Long id;
    private String configKey;
    private String configValue;
    private String description;
}