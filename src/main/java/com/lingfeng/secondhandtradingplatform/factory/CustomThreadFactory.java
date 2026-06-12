package com.lingfeng.secondhandtradingplatform.factory;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class CustomThreadFactory implements ThreadFactory {

    private final String namePrefix;
    private final AtomicInteger counter = new AtomicInteger(1);

    public CustomThreadFactory(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    //创建线程
    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, namePrefix + "-" + counter.getAndIncrement()); // 补上分号

        // 设置未捕获异常处理器
        t.setUncaughtExceptionHandler((thread, ex) -> {
            log.error("线程 {} 执行异常: {}", thread.getName(), ex.getMessage(), ex); // 改进日志格式
        });

        return t;
    }
}