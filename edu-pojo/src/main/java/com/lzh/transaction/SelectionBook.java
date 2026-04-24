package com.lzh.transaction;

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
@TableName("edu_selection_book")
public class SelectionBook implements Serializable {
    @TableId(type = IdType.INPUT)
    private Long id;
    private Long studentId;
    private Long classId;
    // 虽然建表语句没写createTime，如果后续加了可以在这里映射
}