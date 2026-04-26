package com.lzh.entity.academic;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("edu_tag")
public class Tag implements Serializable {
    @TableId(type = IdType.INPUT)
    private Long id;
    private String tagName;
    private Integer tagType;
    private String path;
    private Long parentId;
    private Integer priority;
    private String ruleJson;
    
    @Version
    private Integer version;
}