package com.report.common.util.cache.route;

import com.report.common.util.cache.CacheDef;

/**
 * 缓存存储接口，定义 get/set/del 三个基本操作。
 * <p>
 * 每个 {@link CacheDef} 对应一个缓存值，CacheDef.name 即为存储 key。
 * <p>
 * 实现类：
 * <ul>
 *   <li>{@link LocalStore} - 本地存储</li>
 *   <li>{@link ClusterStore} - 中央存储</li>
 * </ul>
 */
public interface CacheStore {

    /**
     * 获取缓存值
     *
     * @param def 缓存定义，def.name 即为存储 key
     * @return 缓存值，不存在时返回 null
     */
    Object get(CacheDef<?> def);

    /**
     * 写入缓存，已存在则覆盖
     *
     * @param def   缓存定义
     * @param value 缓存值
     */
    void set(CacheDef<?> def, Object value);

    /**
     * 删除缓存条目，不存在时不报错
     *
     * @param def 缓存定义
     */
    void del(CacheDef<?> def);
}
