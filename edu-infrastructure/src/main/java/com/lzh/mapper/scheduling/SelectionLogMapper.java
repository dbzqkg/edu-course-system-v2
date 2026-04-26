package com.lzh.mapper.scheduling;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lzh.entity.audit.SelectionLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 选课日志持久化接口
 * 继承 BaseMapper 后，insert/update/select 等基础功能自动具备
 */
@Mapper
public interface SelectionLogMapper extends BaseMapper<SelectionLog> {
    // 工业级建议：如果有复杂的统计查询（比如查询某课程的抢课热度曲线），再手动写 XML
}