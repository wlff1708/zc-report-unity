package com.report.common.util.cache.route;

import com.report.common.util.cache.CacheDef;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 本地缓存实现，基于 ConcurrentHashMap。
 * <p>
 * 不设过期时间和容量上限，缓存的清除完全由业务代码通过 {@link #del} 主动触发。
 * <p>
 * 线程安全：内部使用 {@link ConcurrentMap}。
 */
public class LocalStore implements CacheStore {

    /** CacheDef.name → 缓存值 */
    private final ConcurrentMap<String, Object> cache = new ConcurrentHashMap<>();

    @Override
    public Object get(CacheDef<?> def) {
        return cache.get(def.getName());
    }

    @Override
    public void set(CacheDef<?> def, Object value) {
        cache.put(def.getName(), value);
    }

    @Override
    public void del(CacheDef<?> def) {
        cache.remove(def.getName());
    }
}
