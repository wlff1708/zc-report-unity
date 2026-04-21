package com.report.module.preload.active.detail;

import cn.hutool.core.util.StrUtil;
import com.report.common.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import java.util.Arrays;

/**
 * 产品模块扫描配置
 * spring.profiles.active 中包含 product-im/product-bm/product-all配置动态配置扫描路径
 */
@Slf4j
@Configuration
public class ModuleLoadSelector implements EnvironmentAware {

    /**
     * 产品类型缓存 - 通过 ProductModuleScanConfig.setEnvironment 初始化
     * 注意：只能在 Spring 容器初始化完成后使用
     */
    private static ProductType productTypeCache;

    @Override
    public void setEnvironment(Environment environment) {
        String productType = Arrays.stream(environment.getActiveProfiles()).filter(profile -> profile.startsWith("product-")).findFirst()
                .map(str -> str.substring("product-".length()))
                .orElseThrow(() -> new IllegalArgumentException("未配置任何product-xxx profile，请检查spring.profiles.active配置"));
        if (StrUtil.isEmpty(productType)) {
            throw new SystemException("产品形态不能为空");
        }
        ProductType productTypeEnum = ProductType.fromCode(productType);
        productTypeCache = productTypeEnum;
        log.info("当前产品形态：{}，将扫描对应模块组件", productTypeEnum);
    }

    public static ProductType getProductType() {
        if (productTypeCache == null) {
            throw new IllegalStateException("ProductType 未初始化，请检查 ProductModuleScanConfig 是否被正确加载");
        }
        return productTypeCache;
    }

    /**
     * IM 模块扫描配置
     */
    @Configuration
    @Profile("product-im")
    @ComponentScan(basePackages = "com.report.module.im")
    @MapperScan(basePackages = "com.report.module.im.mapper")
    public static class IMConfig {
    }

    /**
     * BM 模块扫描配置
     */
    @Configuration
    @Profile("product-bm")
    @ComponentScan(basePackages = "com.report.module.bm")
    @MapperScan(basePackages = "com.report.module.bm.mapper")
    public static class BMConfig {
    }

    /**
     * All 模块扫描配置
     */
    @Configuration
    @Profile("product-all")
    @ComponentScan(basePackages = {"com.report.module"})
    @MapperScan(basePackages = {"com.report.module.im.mapper", "com.report.module.bm.mapper"})
    public static class AllConfig {
    }
}
