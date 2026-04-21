package com.report.module.preload.database;

import cn.hutool.core.collection.ListUtil;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import com.report.module.preload.active.detail.ModuleLoadSelector;
import com.report.module.preload.active.detail.ProductType;
import com.report.module.preload.database.detail.HikariProperties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据库配置类 - 基于策略模式的多数据源管理
 * 负责根据产品形态自动创建数据源并执行 Flyway 迁移
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataBaseConfig {

    @Resource
    private HikariProperties hikariProperties;

    /**
     * 配置动态数据源 Bean
     * 先根据产品形态创建数据库和用户，再配置数据源路由
     * @param properties 动态数据源配置属性
     * @return 动态数据源实例
     */
    @Bean
    public DataSource dataSource(DynamicDataSourceProperties properties) {
        //  获取产品形态
        ProductType productType = ModuleLoadSelector.getProductType();
        log.info("当前产品形态：{}", productType);

        // 提前过滤数据源配置
        updateDynamicDataSourceProperties(properties, productType);

        // 配置数据源路由
        return configDataSource(properties);
    }

    /**
     * 配置动态数据源路由
     * @param properties 动态数据源配置属性（已经过滤完成）
     * @return 动态路由数据源
     */
    private DataSource configDataSource(DynamicDataSourceProperties properties) {
        // 根据过滤后的配置创建数据源提供者
        DynamicDataSourceProvider ourProvider = () -> properties.getDatasource().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> createDataSource(e.getValue(), e.getKey())
                ));

        List<DynamicDataSourceProvider> dataSourceProviderList = ListUtil.toList(ourProvider);
        DynamicRoutingDataSource dataSource = new DynamicRoutingDataSource(dataSourceProviderList);
        // 设置主数据源
        dataSource.setPrimary(properties.getPrimary());
        // 设置数据源切换策略
        dataSource.setStrategy(properties.getStrategy());
        // 是否启用 P6spy SQL 分析
        dataSource.setP6spy(properties.getP6spy());
        return dataSource;
    }


    @Data
    public static class DataSourceConfig {
        /** 连接 URL */
        private String url;
        /** 用户名 */
        private String username;
        /** 密码 */
        private String password;
    }

    /**
     * 根据 DataSourceProperty 创建单个数据源
     */
    private DataSource createDataSource(DataSourceProperty property, String name) {
        DataSourceConfig config = new DataSourceConfig();
        config.setUrl(property.getUrl());
        config.setUsername(property.getUsername());
        config.setPassword(property.getPassword());
        return createBusinessDataSource(config, name);
    }


    /**
     * 创建业务数据源连接池
     * 使用 HikariCP 配置业务数据库连接池
     * @param config 数据源配置（URL、用户名、密码）
     * @param name 数据源名称，用于命名连接池
     * @return Hikari 数据源实例
     */
    public DataSource createBusinessDataSource(DataSourceConfig config, String name) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getUrl());
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        // 应用 HikariCP 连接池参数
        hikariConfig.setMinimumIdle(hikariProperties.getMinimumIdle());           // 最小空闲连接数
        hikariConfig.setMaximumPoolSize(hikariProperties.getMaximumPoolSize());   // 最大连接池大小
        hikariConfig.setIdleTimeout(hikariProperties.getIdleTimeout());           // 连接空闲超时时间
        hikariConfig.setMaxLifetime(hikariProperties.getMaxLifetime());           // 连接最大生命周期
        hikariConfig.setConnectionTimeout(hikariProperties.getConnectionTimeout()); // 连接超时时间
        hikariConfig.setConnectionTestQuery(hikariProperties.getConnectionTestQuery()); // 连接测试查询
        hikariConfig.setPoolName(name + "HikariCP");  // 设置连接池名称便于监控
        return new HikariDataSource(hikariConfig);
    }

    /**
     * 根据产品形态更新数据源配置，过滤掉不需要的数据源
     * @param properties 原始配置
     * @param productType 产品形态
     */
    private void updateDynamicDataSourceProperties(DynamicDataSourceProperties properties, ProductType productType) {
        // 原数据源配置
        Map<String, DataSourceProperty> allProperties = properties.getDatasource();
        // 过滤之后的数据源配置
        Map<String, DataSourceProperty> filteredProperties = new HashMap<>();

        switch (productType) {
            case IM -> {
                if (allProperties.containsKey("master")) {
                    filteredProperties.put("master", allProperties.get("master"));
                }
            }
            case BM -> {
                if (allProperties.containsKey("slave")) {
                    filteredProperties.put("slave", allProperties.get("slave"));
                }
            }
            case ALL -> filteredProperties.putAll(allProperties);
        }

        // 直接修改原 properties 对象，使用过滤后的配置
        properties.setDatasource(filteredProperties);
        log.info("提前过滤数据源配置，保留数据源：{}", filteredProperties.keySet());
    }
}
