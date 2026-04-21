package com.report.module.im.business;

import com.report.module.im.pojo.bo.ImFileDealBO;
import com.report.module.im.pojo.bo.ImFileTopicMsgBO;
import com.report.module.im.pojo.converter.ImKafkaFileConverter;
import com.report.module.im.pojo.dto.ImKafkaListenDataDTO;
import com.report.module.im.service.ImHandleFileTopicStrategy;
import com.report.module.im.service.impl.ImHandleFileTopicStrategyFactory;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件类 Kafka 业务处理层
 */
@Component
public class ImKafkaFileBusiness {

    @Resource
    private ImHandleFileTopicStrategyFactory imHandleFileTopicStrategyFactory;

    @Resource
    private ImKafkaFileConverter imKafkaFileConverter;

    public void handle(List<ImKafkaListenDataDTO> dataList) {
        // DTO → BO 按父模块分组并封装为 IMFileDealBO 列表
        List<ImFileDealBO> dealList = imKafkaFileConverter.toFileTopicMsgBOList(dataList).stream()
                .collect(Collectors.groupingBy(ImFileTopicMsgBO::getModule))
                .entrySet().stream()
                .map(entry -> new ImFileDealBO(entry.getKey(), entry.getValue()))
                .toList();

        // 按模块分策略处理
        dealList.forEach(deal -> {
            ImHandleFileTopicStrategy strategy = imHandleFileTopicStrategyFactory.getStrategy(deal.module());
            strategy.handle(deal);
        });
    }
}
