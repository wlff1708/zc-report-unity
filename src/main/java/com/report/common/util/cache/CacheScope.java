package com.report.common.util.cache;

/**
 * 缓存存储范围，决定数据存放在本地还是中央缓存。
 * <p>
 * LOCAL   - 本地缓存（Caffeine 实现），仅当前 JVM 进程可见。
 *           适合不随节点变化的数据，如字典、配置、目录列表等。
 * <p>
 * CLUSTER - 中央缓存（Redis 实现），集群所有节点共享读写。
 *           适合需要跨节点同步的状态数据，如会话、认证状态、分布式锁标记等。
 */
public enum CacheScope {
    LOCAL,
    CLUSTER
}
