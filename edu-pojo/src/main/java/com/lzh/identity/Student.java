package com.lzh.identity;

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
@TableName("edu_student")
public class Student implements Serializable {
    @TableId(type = IdType.INPUT)
    private Long id;
    private String studentNo;
    private String name;
    private Long majorId;
    private Integer grade;
    private String engLevel;

    // 1024位时间位图优化为二进制存储
    private byte[] scheduleBitMap;

    private Integer tuitionStatus;
    private Integer evalStatus;

    @Version
    private Integer version;
}
