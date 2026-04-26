package com.lzh.mapper.transaction;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lzh.entity.transaction.SelectionResult;
import org.apache.ibatis.annotations.Mapper;

/**
 * 选课结果持久化接口
 * 属于“选课域 (Transaction)”在基础设施层的实现
 */
@Mapper
public interface SelectionResultMapper extends BaseMapper<SelectionResult> {

    /**
     * 工业级标准：封装常用的业务查询，保证 Service 层逻辑纯粹
     * 根据学生ID和班级ID获取唯一的选课结果（含时间位图快照）
     */
    default SelectionResult selectOne(Long studentId, Long classId) {
        return this.selectOne(new LambdaQueryWrapper<SelectionResult>()
                .eq(SelectionResult::getStudentId, studentId)
                .eq(SelectionResult::getClassId, classId)
                .eq(SelectionResult::getSelectionStatus, 1)); // 仅查询有效状态
    }
}