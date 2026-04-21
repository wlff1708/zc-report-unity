package com.report.common.util.redis;

import com.report.common.exception.ThirdPartyException;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Redis 操作工具类
 * 支持字符串、List、Hash、Set、ZSet 等数据类型操作
 * 支持选择数据库
 */
@Component
public class RedisUtil {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // ==================== 字符串操作 ====================
    /**
     * 获取字符串值
     */
    public String get(String key) {
        try {
            return stringRedisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            throw new ThirdPartyException("Redis获取字符串值失败：" + e.getMessage(), e);
        }
    }

    /**
     * 设置字符串值
     */
    public void set(String key, String value) {
        try {
            stringRedisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            throw new ThirdPartyException("Redis设置字符串值失败：" + e.getMessage(), e);
        }
    }

    /**
     * 删除key
     */
    public void del(String key) {
        try {
            stringRedisTemplate.delete(key);
        } catch (Exception e) {
            throw new ThirdPartyException("Redis删除字符串失败：" + e.getMessage(), e);
        }
    }


    /**
     * 原子性地设置键值对（SETNX 操作）
     * <p>
     * 仅当 key 不存在时才设置值，并同时设置过期时间。
     * 该操作是原子的，常用于实现分布式锁或幂等性控制。
     * </p>
     *
     * @param key   Redis 键
     * @param value 要设置的值
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return 设置成功返回 true，若 key 已存在则返回 false
     */
    public boolean setNx(String key, String value, long timeout, TimeUnit unit) {
        try {
            Boolean acquired = stringRedisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
            return Boolean.TRUE.equals(acquired);
        } catch (Exception e) {
            throw new ThirdPartyException("字符串不存在就设置操作失败：" + e.getMessage(), e);
        }
    }
    // ==================== List 操作 ====================



    // ==================== Hash 操作 ====================




    // ==================== Set 操作 ====================


    // ==================== ZSet 操作 ====================


    // ==================== lua脚本操作 ====================

    /**
     * 执行 Lua 脚本
     * <p>
     * 脚本中通过 KEYS[1] 访问 key，通过 ARGV[1] 访问 value
     * </p>
     *
     * @param script Lua 脚本
     * @param key    操作的 Redis 键（KEYS[1]）
     * @param value  脚本参数值（ARGV[1]）
     * @return 脚本返回值是否大于 0
     */
    public boolean executeScript(String script, String key, String value) {
        try {
            var redisScript = new DefaultRedisScript<>(script, Long.class);
            Long result = stringRedisTemplate.execute(redisScript, Collections.singletonList(key), value);
            return result > 0;
        } catch (Exception e) {
            throw new ThirdPartyException("执行lua脚本失败：" + e.getMessage(), e);
        }
    }
}
