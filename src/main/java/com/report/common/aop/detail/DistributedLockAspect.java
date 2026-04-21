package com.report.common.aop.detail;


import com.report.common.aop.DistributedLockAnnotation;
import com.report.common.exception.SystemException;
import com.report.common.util.redis.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁 AOP 切面
 * <p>
 * 拦截 {@link DistributedLockAnnotation} 注解，自动获取和释放分布式锁
 * </p>
 * <p>
 * Key 命名规则：lock:{product}:{scene}
 * 例如：lock:im:sync_device
 * </p>
 *
 * @author zcenter
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAspect {

    private final RedisUtil redisUtil;

    /**
     * 锁的 Redis Key 前缀
     */
    private static final String LOCK_PREFIX = "lock:";

    /**
     * 环绕通知：获取锁 → 执行方法 → 释放锁
     */
    @Around("@annotation(com.report.common.aop.DistributedLockAnnotation)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLockAnnotation annotation = method.getAnnotation(DistributedLockAnnotation.class);

        // 构建锁的 Key: lock:{product}:{scene}
        String lockKey = buildLockKey(annotation.product(), annotation.scene());
        String lockId = UUID.randomUUID().toString();

        long timeout = annotation.timeout();
        TimeUnit unit = annotation.unit();

        // 尝试获取锁（失败直接返回）
        boolean acquired = redisUtil.setNx(lockKey, lockId, timeout, unit);

        if (!acquired) {
            String msg = String.format("获取分布式锁失败：key=%s", lockKey);
            log.warn(msg);
            throw new SystemException(msg);
        }

        log.debug("获取分布式锁成功：key={}, lockId={}", lockKey, lockId);

        try {
            // 执行目标方法
            return joinPoint.proceed();
        } finally {
            // 确保释放锁
            releaseLock(lockKey, lockId);
        }
    }

    /**
     * 构建锁的 Redis Key
     * <p>
     * 格式：lock:{product}:{scene}
     * </p>
     *
     * @param product 产品形态
     * @param scene   业务场景
     * @return Redis Key
     */
    private String buildLockKey(String product, String scene) {
        return LOCK_PREFIX + product + ":" + scene;
    }

    /**
     * 释放锁
     *
     * @param lockKey 锁的 Key
     * @param lockId  锁的唯一标识
     */
    private void releaseLock(String lockKey, String lockId) {
        String luaScript = """
            if redis.call('get', KEYS[1]) == ARGV[1] then
                return redis.call('del', KEYS[1])
            else
                return 0
            end
            """;

        boolean released = redisUtil.executeScript(luaScript, lockKey, lockId);

        if (released) {
            log.debug("释放分布式锁成功：key={}, lockId={}", lockKey, lockId);
        } else {
            log.warn("释放分布式锁失败（可能已过期）：key={}, lockId={}", lockKey, lockId);
        }
    }
}
