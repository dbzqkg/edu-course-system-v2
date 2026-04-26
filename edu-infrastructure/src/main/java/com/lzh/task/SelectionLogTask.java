package com.lzh.task;

import com.alibaba.fastjson2.JSON; // 引入 Fastjson2
import com.lzh.entity.audit.SelectionLog;
import com.lzh.mapper.scheduling.SelectionLogMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SelectionLogTask implements Runnable {
    private final SelectionLog selectionLog;
    private final SelectionLogMapper selectionLogMapper;

    @Override
    public void run() {
        selectionLogMapper.insert(selectionLog);
    }

    /**
     * 工业级改进：使用 Fastjson2 序列化
     * 这样当拒绝策略触发时，磁盘日志里记录的是完美的 JSON 字符串
     */
    @Override
    public String toString() {
        // 使用 fastjson2 序列化整个对象，简单、准确、包含所有字段
        return JSON.toJSONString(this.selectionLog);
    }
}