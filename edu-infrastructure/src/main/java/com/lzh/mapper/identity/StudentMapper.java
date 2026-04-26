package com.lzh.mapper.identity;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lzh.entity.identity.Student;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {
}
