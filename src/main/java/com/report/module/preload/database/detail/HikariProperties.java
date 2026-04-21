package com.report.module.preload.database.detail;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * HikariCP 连接池配置属性
 */
@Data
@Component
public class HikariProperties {

    /** 最小空闲连接数量 */
    @Value("${spring.datasource.hikari.minimum-idle:30}")
    private int minimumIdle;

    /** 连接池最大连接数 */
    @Value("${spring.datasource.hikari.maximum-pool-size:150}")
    private int maximumPoolSize;

    /** 空闲连接存活最大时间（毫秒） */
    @Value("${spring.datasource.hikari.idle-timeout:180000}")
    private long idleTimeout;

    /** 连接池最长生命周期（毫秒） */
    @Value("${spring.datasource.hikari.max-lifetime:1800000}")
    private long maxLifetime;

    /** 连接超时时间（毫秒） */
    @Value("${spring.datasource.hikari.connection-timeout:30000}")
    private long connectionTimeout;

    /** 连接测试查询 */
    @Value("${spring.datasource.hikari.connection-test-query:SELECT 1}")
    private String connectionTestQuery;
}
