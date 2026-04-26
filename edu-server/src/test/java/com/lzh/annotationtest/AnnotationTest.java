package com.lzh.annotationtest;

import com.lzh.EduServerApplication;
import com.lzh.annotations.LogSelection;
import com.lzh.context.StuContext;
import com.lzh.service.ClassService;
import com.lzh.util.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = EduServerApplication.class)
@Slf4j
public class AnnotationTest {

    @Autowired
    private ClassService classService;

    @BeforeEach
    void setUp() {
        // 模拟登录：填充 ThreadLocal 上下文
        // 这是 SelectionLogFactory 正常工作的前提
        StuContext mockStu = new StuContext(
                20240001L, "测试学生", 1L, 1, "A", null, 1, 1
        );
        ThreadLocalUtil.set(mockStu);
    }

    @AfterEach
    void tearDown() {
        // 测试完清理，防止污染其他测试
        ThreadLocalUtil.remove();
    }

    /**
     * 测试核心 AOP 拦截逻辑
     * 注意：切面里用了 args[0]，所以这里必须传一个 Long 类型的 classId
     */
    @Test
    public void testAuditFlow() throws InterruptedException {
        log.info(">>> 开始模拟抢课动作...");

        // 模拟调用逻辑
        classService.selectClass(1001L);

        log.info(">>> 抢课逻辑执行完毕，主线程准备返回。");

        // 由于落库是异步的 (@Async)，我们需要让主线程等一会儿，
        // 否则测试进程直接结束，你看不到异步线程打印的日志
        Thread.sleep(2000);
    }
}