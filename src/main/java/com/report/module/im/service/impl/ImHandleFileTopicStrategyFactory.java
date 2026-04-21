package com.report.module.im.service.impl;

import com.report.module.im.service.ImHandleFileTopicStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 告警存储策略工厂
 * <p>
 * 启动时自动收集所有 {@link ImHandleFileTopicStrategy} 实现，
 * 按 {@link ImHandleFileTopicStrategy#supportedModules()} 建立映射。
 * 未匹配的 module 走 {@link ImDefaultAlarmFileTopicHandleStrategy}。
 */
@Slf4j
@Component
public class ImHandleFileTopicStrategyFactory {

    /** module → 策略实例 */
    private final Map<String, ImHandleFileTopicStrategy> strategyMap = new HashMap<>();

    /** 默认策略 */
     private final ImHandleFileTopicStrategy defaultStrategy;

    public ImHandleFileTopicStrategyFactory(List<ImHandleFileTopicStrategy> strategies,
                                            ImDefaultAlarmFileTopicHandleStrategy defaultStrategy) {
        this.defaultStrategy = defaultStrategy;
        for (ImHandleFileTopicStrategy strategy : strategies) {
            if (strategy == defaultStrategy) {
                continue;
            }
            for (String module : strategy.supportedModules()) {
                strategyMap.put(module, strategy);
            }
        }
        log.info("文件类消息处理策略加载完成: 已注册模块={}", strategyMap.keySet());
    }

    /**
     * 根据 module 获取对应策略，未匹配时返回默认策略
     */
    public ImHandleFileTopicStrategy getStrategy(String moduleType) {
        return strategyMap.getOrDefault(moduleType, defaultStrategy);
    }
}
