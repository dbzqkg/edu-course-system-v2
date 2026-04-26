package com.lzh.entity.scheduling;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.lzh.vo.TimeBitmap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("edu_class")
public class EduClass implements Serializable {
    @TableId(type = IdType.INPUT)
    private Long id;
    private String classNo;
    private Long courseId;
    private String teacherName;
    private Integer maxCapacity;
    private Integer currentStock;
    
    // 1024位时间冲突位图
    private TimeBitmap timeBitmap;
    
    private String location;

    @Version // 核心新增：MyBatis-Plus 会在更新时执行 set version = version + 1 where version = ?
    private Integer version;
}