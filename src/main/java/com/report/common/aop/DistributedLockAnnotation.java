package com.report.common.aop;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁注解
 * <p>
 * 使用方式：
 * </p>
 * <pre>{@code
 * @DistributedLock(product = "im", scene = "sync_device", timeout = 60L)
 * public void syncDevice(String deviceId) {
 *     // 业务逻辑，方法执行完自动释放锁
 * }
 * }</pre>
 *
 * @author zcenter
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLockAnnotation {

    /**
     * 产品形态（必填）
     * <p>
     * 例如：im（接入口检测）、bm（出口保密监测）、all（统一管理）
     * </p>
     */
    String product();

    /**
     * 业务场景名（必填）
     * <p>
     * 例如：sync_device（设备同步）、update_config（配置更新）
     * </p>
     */
    String scene();

    /**
     * 锁超时时间（秒），默认 30 秒
     * <p>
     * 超时后锁自动释放，无需手动续期
     * </p>
     */
    long timeout() default 30L;

    /**
     * 时间单位，默认秒
     */
    TimeUnit unit() default TimeUnit.SECONDS;
}
