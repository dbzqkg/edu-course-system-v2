package com.lzh.entity.identity;

import com.baomidou.mybatisplus.annotation.*;
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
@TableName("edu_student")
public class Student implements Serializable {
    @TableId(type = IdType.INPUT)
    private Long id;
    private String studentNo;
    private String name;
    private Long majorId;
    private Integer grade;
    private String engLevel;

    private TimeBitmap scheduleBitMap;

    private Integer tuitionStatus;
    private Integer evalStatus;

    @Version
    private Integer version;
}
