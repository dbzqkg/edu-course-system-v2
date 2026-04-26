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
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("edu_course")
public class Course implements Serializable {
    @TableId(type = IdType.INPUT)
    private Long id;
    private String courseNo;
    private String name;
    private BigDecimal credits;
    private Integer courseType;
    private String tagSet;
    
    @Version
    private Integer version;
}