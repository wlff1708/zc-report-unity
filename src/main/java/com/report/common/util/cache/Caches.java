package com.report.common.util.cache;


import com.report.common.util.cache.route.CacheStore;
import com.report.common.util.cache.route.LocalStore;
import com.report.common.util.cache.route.ClusterStore;
import com.report.common.util.redis.RedisUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * 统一缓存门面，提供全局静态方法 get/set/del。
 * <p>
 * 每个 {@link CacheDef} 对应一个缓存值，CacheDef.name 即为存储 key。
 * CacheDef 的泛型 T 声明值类型，get 时自动推断返回类型。
 * <p>
 * 使用方式（无需注入，直接静态调用）：
 * <pre>
 *   Caches.get(CacheKeysName.S2_STATUS);                // → Boolean
 *   Caches.set(CacheKeysName.S2_STATUS, true);           // 存
 *   Caches.del(CacheKeysName.S2_STATUS);                 // 删
 *   Caches.get(CacheKeysName.ALARM_DATA_TOPICS);         // → List&lt;String&gt;
 * </pre>
 * <p>
 * 内部根据 CacheDef 的 scope 自动路由到 CaffeineStore 或 RedisStore，
 * 调用方无需关心底层存储实现。
 * <p>
 * 注意：不能在 Spring 容器初始化完成前（@PostConstruct 之前）调用，否则 SELF 为 null。
 */
@Component
public class Caches {

    /** 存储实现映射：LOCAL → CaffeineStore，CLUSTER → RedisStore */
    private final EnumMap<CacheScope, CacheStore> stores;

    /** 静态自引用，@PostConstruct 时赋值，供静态方法使用 */
    private static Caches SELF;

    /**
     * 构造时创建两个 Store 实例并注册到路由表。
     * 由 Spring 自动调用，注入 JedisUtil 用于 RedisStore。
     *
     * @param jedisUtil Redis 操作工具，由 manage-domain 提供
     */
    public Caches(RedisUtil jedisUtil) {
        this.stores = new EnumMap<>(Map.of(
                CacheScope.LOCAL, new LocalStore(),
                CacheScope.CLUSTER, new ClusterStore(jedisUtil)
        ));
    }

    /** Spring 容器初始化完成后将自身引用赋给静态字段 */
    @PostConstruct
    void init() {
        SELF = this;
    }

    /**
     * 获取缓存值，返回类型由 CacheDef 的泛型 T 决定
     *
     * @param def 缓存定义，泛型 T 声明值类型
     * @return 缓存值，不存在时返回 null
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(CacheDef<T> def) {
        return (T) SELF.stores.get(def.getScope()).get(def);
    }

    /**
     * 写入缓存，已存在则覆盖
     *
     * @param def   缓存定义
     * @param value 缓存值
     */
    public static <T> void set(CacheDef<T> def, T value) {
        SELF.stores.get(def.getScope()).set(def, value);
    }

    /**
     * 删除缓存条目
     *
     * @param def 缓存定义
     */
    public static <T> void del(CacheDef<T> def) {
        SELF.stores.get(def.getScope()).del(def);
    }

}
