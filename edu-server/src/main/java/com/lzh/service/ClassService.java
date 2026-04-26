package com.lzh.service;

import com.lzh.annotations.LogSelection;
import com.lzh.context.StuContext;
import com.lzh.util.ThreadLocalUtil;
import com.lzh.vo.TimeBitmap;
import org.springframework.stereotype.Service;

@Service
public class ClassService {

    @LogSelection(value = "模拟选课", sourceType = 2, key = "classId")
    public void selectClass(Long classId) {
        System.out.println("执行真正的选课逻辑...");
    }
}
