package com.report.module.preload.active;

import com.report.module.preload.active.detail.ModuleLoadSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用产品模块扫描注解
 * 根据激活的 product-xxx profile 动态扫描对应模块的 Bean 和 Mapper
 *
 * 使用方式：在主启动类上添加此注解
 * - product-im: 仅扫描 IM 模块
 * - product-bm: 仅扫描 BM 模块
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ModuleLoadSelector.class)
public @interface EnableModuleLoad {
}
