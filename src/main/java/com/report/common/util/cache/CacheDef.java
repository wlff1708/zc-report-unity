package com.report.common.util.cache;

import lombok.Getter;

/**
 * 缓存定义，不可变对象。
 * <p>
 * 泛型 T 声明该缓存项的值类型，在 CacheNames 定义时指定，调用 Caches.get 时自动推断返回类型。
 * <p>
 * 使用示例：
 * <pre>
 *   CacheDef&lt;String&gt; status = CacheDef.cluster("s2:status");
 *   CacheDef&lt;List&lt;String&gt;&gt; topics = CacheDef.local("alarm:topics");
 * </pre>
 */
@Getter
public class CacheDef<T> {

    /** 缓存名称，唯一标识一个缓存值，同时作为实际存储的 key（如 "s2:status"） */
    private final String name;

    /** 存储范围，决定路由到 CaffeineStore 还是 RedisStore */
    private final CacheScope scope;

    private CacheDef(String name, CacheScope scope) {
        this.name = name;
        this.scope = scope;
    }

    /**
     * 创建本地缓存定义，值存储在当前 JVM 内存中
     *
     * @param name 缓存名称，建议使用冒号分层（如 "alarm:data:topics"）
     */
    public static <T> CacheDef<T> local(String name) {
        return new CacheDef<>(name, CacheScope.LOCAL);
    }

    /**
     * 创建中央缓存定义，值存储在 Redis，集群所有节点共享
     *
     * @param name 缓存名称，建议使用冒号分层（如 "s2:status"）
     */
    public static <T> CacheDef<T> cluster(String name) {
        return new CacheDef<>(name, CacheScope.CLUSTER);
    }
}
