package com.report.module.im.service;

import com.report.module.im.pojo.bo.ImDataDealBO;
import com.report.module.im.service.impl.ImHandleDataTopicStrategyFactory;

import java.util.List;

/**
 * 数据类消息处理策略
 *
 * 不同 module 的落盘规则不同，
 * 每种规则实现此接口，由 {@link ImHandleDataTopicStrategyFactory} 按 module 路由。
 */
public interface ImHandleDataTopicStrategy {

    /**
     * 该策略支持的模块类型列表，用于工厂路由。
     * 默认返回空列表（即兜底策略，不参与 module 路由）。
     * 具名策略需覆盖此方法返回对应的 module 列表。
     */
    default List<String> supportedModules() {
        return List.of();
    }

    /**
     * 处理数据类消息
     * @param deal 数据处理业务对象（包含模块类型和该模块下的所有消息）
     */
    void handle(ImDataDealBO deal);
}
