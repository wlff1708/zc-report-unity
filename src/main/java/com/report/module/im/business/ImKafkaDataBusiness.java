package com.report.module.im.business;

import com.report.module.im.pojo.bo.ImDataDealBO;
import com.report.module.im.pojo.bo.ImDataTopicMsgBO;
import com.report.module.im.pojo.converter.ImKafkaDataConverter;
import com.report.module.im.pojo.dto.ImKafkaListenDataDTO;
import com.report.module.im.service.ImHandleDataTopicStrategy;
import com.report.module.im.service.impl.ImHandleDataTopicStrategyFactory;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据类 Kafka 业务处理层
 */
@Component
public class ImKafkaDataBusiness {

    @Resource
    private ImHandleDataTopicStrategyFactory imHandleDataTopicStrategyFactory;

    @Resource
    private ImKafkaDataConverter imKafkaDataConverter;

    public void handle(List<ImKafkaListenDataDTO> dataList) {
        // DTO → BO 按父模块分组并封装为 ImDataDealBO 列表
        List<ImDataDealBO> dealList = imKafkaDataConverter.toDataTopicMsgBOList(dataList).stream()
                .collect(Collectors.groupingBy(ImDataTopicMsgBO::getModule))
                .entrySet().stream()
                .map(entry -> new ImDataDealBO(entry.getKey(), entry.getValue()))
                .toList();

        // 按模块分策略处理
        dealList.forEach(deal -> {
            ImHandleDataTopicStrategy strategy = imHandleDataTopicStrategyFactory.getStrategy(deal.module());
            strategy.handle(deal);
        });
    }
}
