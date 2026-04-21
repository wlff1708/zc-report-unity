package com.report.common.config.schedule;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTask;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 时钟回拨任务检测补偿类
 * 检测系统时钟变化(回拨/快进), 自动重置@Scheduled定时任务
 */
//@Component
@Slf4j
public class ClockSkewTaskCompensator {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final long CLOCK_THRESHOLD_MS = 5000L;
    private static final long DETECT_INTERVAL_MS = 1000L;

    private final AtomicLong initialTimeDiff = new AtomicLong(0);
    private ScheduledExecutorService detectorExecutor;

    private final ScheduledAnnotationBeanPostProcessor scheduledProcessor;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final ApplicationContext applicationContext;

    public ClockSkewTaskCompensator(ScheduledAnnotationBeanPostProcessor scheduledProcessor,
                                    ThreadPoolTaskScheduler taskScheduler,
                                    ApplicationContext applicationContext) {
        this.scheduledProcessor = scheduledProcessor;
        this.taskScheduler = taskScheduler;
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        initialTimeDiff.set(calculateTimeDiff());

        detectorExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "clock-detector");
            t.setDaemon(true);
            return t;
        });

        detectorExecutor.scheduleAtFixedRate(this::detectClockChange,
            DETECT_INTERVAL_MS, DETECT_INTERVAL_MS, TimeUnit.MILLISECONDS);

        log.info("[时钟检测补偿] 启动成功, 检测间隔: " + DETECT_INTERVAL_MS + "ms, 阈值: " + CLOCK_THRESHOLD_MS + "ms");
    }

    @PreDestroy
    public void destroy() {
        if (detectorExecutor != null) {
            detectorExecutor.shutdownNow();
        }
        log.info("[时钟检测补偿] 已关闭");
    }

    private long calculateTimeDiff() {
        return (System.nanoTime() / 1_000_000) - System.currentTimeMillis();
    }

    private void detectClockChange() {
        try {
            long currentDiff = calculateTimeDiff();
            long change = currentDiff - initialTimeDiff.get();

            if (Math.abs(change) > CLOCK_THRESHOLD_MS) {
                String direction = change > 0 ? "回拨" : "调快";
                log.warn("\n========================================");
                log.warn("[时钟检测] 系统时钟被" + direction + " " + Math.abs(change) / 1000 + "秒");
                log.warn("检测时间: " + DATE_FORMAT.format(new Date()));
                log.warn("========================================");

                initialTimeDiff.set(currentDiff);
                resetAllScheduledTasks();
            }
        } catch (Exception e) {
            log.error("[时钟检测] 异常: " + e.getMessage());
        }
    }

    /**
     * 重置所有定时任务: 取消旧任务 -> 重新注册 -> 清空旧队列
     * 注意顺序：必须先重新注册再清队列，否则新任务也会被清掉
     */
    private synchronized void resetAllScheduledTasks() {
        try {
            log.warn("[任务补偿] 开始重置所有定时任务...");

            int canceledCount = cancelAllTasksAndReschedule();
            log.warn("[任务补偿] 完成，共处理 " + canceledCount + " 个任务\n");
        } catch (Exception e) {
            throw new RuntimeException("[任务补偿] 失败: " + e.getMessage());
        }
    }



    /**
     * 取消所有任务并触发重新调度
     * 通过遍历所有Bean重新处理@Scheduled注解
     */
    @SuppressWarnings("unchecked")
    private int cancelAllTasksAndReschedule() {
        int count = 0;
        try {
            Field tasksField = ScheduledAnnotationBeanPostProcessor.class
                .getDeclaredField("scheduledTasks");
            tasksField.setAccessible(true);
            Object tasksObj = tasksField.get(scheduledProcessor);

            if (tasksObj instanceof Map) {
                Map<Object, Set<ScheduledTask>> tasksMap =
                    (Map<Object, Set<ScheduledTask>>) tasksObj;
                
                // 取消所有任务
                for (Set<ScheduledTask> taskSet : tasksMap.values()) {
                    for (ScheduledTask task : taskSet) {
                        task.cancel();
                        count++;
                    }
                }
                
                log.warn("[任务补偿] 取消 " + count + " 个旧任务");
                
                // 清空Map
                tasksMap.clear();
                
                // 重新扫描所有Bean的@Scheduled方法
                String[] beanNames = applicationContext.getBeanDefinitionNames();
                for (String beanName : beanNames) {
                    try {
                        Object bean = applicationContext.getBean(beanName);
                        scheduledProcessor.postProcessAfterInitialization(bean, beanName);
                    } catch (Exception ignored) {
                        // 忽略无法获取的Bean
                    }
                }
                
                log.warn("[任务补偿] 重新注册完成");
            }
        } catch (Exception e) {
            throw new RuntimeException("[任务补偿] 异常: " + e.getMessage());
        }
        return count;
    }
}
