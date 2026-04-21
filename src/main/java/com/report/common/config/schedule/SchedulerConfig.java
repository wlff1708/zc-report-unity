package com.report.common.config.schedule;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Spring 定时任务调度器配置
 * 
 * 注意: @EnableScheduling 应放在启动类或专门的配置类, 避免重复
 */
//@Configuration
public class SchedulerConfig {
    
    /**
     * 自定义 ThreadPoolTaskScheduler
     * 用于支持任务重置时获取底层线程池
     */
    @Bean
    @Primary
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("scheduled-task-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60);
        // 设置移除取消的任务策略
        scheduler.setRemoveOnCancelPolicy(true);
        // 设置错误处理器
        scheduler.setErrorHandler(t -> {
            System.err.println("[定时任务异常] " + t.getMessage());
            t.printStackTrace();
//             可扩展：发送告警、记录到数据库等
//             alertService.sendAlert("定时任务异常", t);
        });
        return scheduler;
    }
}