package com.report.module.im.service.impl;

import com.report.module.im.service.ImHandleDataTopicStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据类消息处理策略工厂
 * <p>
 * 启动时自动收集所有 {@link ImHandleDataTopicStrategy} 实现，
 * 按 {@link ImHandleDataTopicStrategy#supportedModules()} 建立映射。
 * 未匹配的 module 走 {@link ImDefaultAlarmDataTopicHandleStrategy}。
 */
@Slf4j
@Component
public class ImHandleDataTopicStrategyFactory {

    /** module → 策略实例 */
    private final Map<String, ImHandleDataTopicStrategy> strategyMap = new HashMap<>();

    /** 默认策略 */
    private final ImHandleDataTopicStrategy defaultStrategy;

    public ImHandleDataTopicStrategyFactory(List<ImHandleDataTopicStrategy> strategies,
                                            ImDefaultAlarmDataTopicHandleStrategy defaultStrategy) {
        this.defaultStrategy = defaultStrategy;
        for (ImHandleDataTopicStrategy strategy : strategies) {
            if (strategy == defaultStrategy) {
                continue;
            }
            for (String module : strategy.supportedModules()) {
                strategyMap.put(module, strategy);
            }
        }
        log.info("数据类消息处理策略加载完成: 已注册模块={}", strategyMap.keySet());
    }

    /**
     * 根据 module 获取对应策略，未匹配时返回默认策略
     */
    public ImHandleDataTopicStrategy getStrategy(String moduleType) {
        return strategyMap.getOrDefault(moduleType, defaultStrategy);
    }
}
