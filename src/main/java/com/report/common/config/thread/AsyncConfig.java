package com.report.common.config.thread;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 自定义线程池
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("testExecutor")
    public Executor testExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 设置核心线程数
        executor.setCorePoolSize(5);
        // 设置最大线程数
        executor.setMaxPoolSize(10);
        // 设置队列大小
        executor.setQueueCapacity(100);
        // 设置非核心线程多久回收
        executor.setKeepAliveSeconds(60);
        // 设置线程名称前缀
        executor.setThreadNamePrefix("test-async-");
        // 初始化线程池
        executor.initialize();
        return executor;
    }
}
