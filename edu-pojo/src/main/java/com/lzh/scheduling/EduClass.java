package com.lzh.scheduling;

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
    private byte[] timeBitmap;
    
    private String location;
}