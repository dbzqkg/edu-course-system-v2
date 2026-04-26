package com.lzh.identity;

import com.lzh.entity.identity.Student;
import com.lzh.entity.transaction.SelectionResult;
import com.lzh.enums.AppResultCode;
import com.lzh.exception.BusinessException;
import com.lzh.mapper.identity.StudentMapper;
import com.lzh.mapper.transaction.SelectionResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 学生领域服务：处理核心位图变更逻辑
 */
@Service
public class StudentDomainService {

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private SelectionResultMapper selectionResultMapper;

    /**
     * 工业级安全退课逻辑
     * @param studentId 学生ID
     * @param classId 班级ID
     */
    // TODO 我还没打算写呢
//    @Transactional(rollbackFor = Exception.class)
//    public void dropClassSafely(Long studentId, Long classId) {
//        // 1. 获取选课记录，必须拿到当时的快照
//        SelectionResult result = selectionResultMapper.selectOne(studentId, classId);
//        if (result == null || result.getSnapshotTimeBitmap() == null) {
//            throw new BusinessException(AppResultCode.PARAM_ERROR, "未找到有效的选课记录");
//        }
//
//        // 2. 锁定学生记录 (使用乐观锁 version 机制) [cite: 5, 8]
//        Student student = studentMapper.selectById(studentId);
//
//        // 3. 执行“安全剔除”
//        // 使用 result 中的快照而非当前课程表的实时数据！
//        student.getScheduleBitMap().dropClass(result.getSnapshotTimeBitmap());
//
//        // 4. 写回数据库
//        // MyBatis-Plus 会自动根据 @Version 字段处理冲突
//        int rows = studentMapper.updateById(student);
//        if (rows == 0) {
//            throw new BusinessException(AppResultCode.DB_WRITE_ERROR, "退课并发冲突，请重试");
//        }
//
//        // 5. 逻辑删除或物理删除选课结果
//        selectionResultMapper.deleteById(result.getId());
//    }
}