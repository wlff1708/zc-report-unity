package com.report.common.util.cache.route;


import com.alibaba.fastjson.JSON;
import com.report.common.util.cache.CacheDef;
import com.report.common.util.redis.RedisUtil;
import lombok.RequiredArgsConstructor;

/**
 * 中央缓存实现，基于 Redis。
 * <p>
 * 通过 {@link RedisUtil} 操作 Redis，不设 TTL（永不过期），
 * 缓存的清除完全由业务代码通过 {@link #del} 主动触发。
 * <p>
 * Redis key 即为 CacheDef.name（如 "s2:status"），
 * 值的序列化/反序列化统一使用 JSON（Fastjson）。
 */
@RequiredArgsConstructor
public class ClusterStore implements CacheStore {

    /** Redis 操作工具，由 Spring 注入 */
    private final RedisUtil redisUtil;

    @Override
    public Object get(CacheDef<?> def) {
        String raw = redisUtil.get(def.getName());
        if (raw == null) {
            return null;
        }
        return JSON.parse(raw);
    }

    @Override
    public void set(CacheDef<?> def, Object value) {
        redisUtil.set(def.getName(), JSON.toJSONString(value));
    }

    @Override
    public void del(CacheDef<?> def) {
        redisUtil.del(def.getName());
    }
}
