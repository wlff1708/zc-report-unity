package com.report.module.im.listener;

import com.report.common.util.cache.CacheDef;
import com.report.module.im.business.ImKafkaDataBusiness;
import com.report.module.im.constants.ImCacheKeysName;
import com.report.module.im.pojo.bo.ImTopicInfoBO;
import com.report.module.im.pojo.dto.ImKafkaListenDataDTO;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.kafka.listener.MessageListenerContainer;

import java.util.List;
import java.util.function.Predicate;

/**
 * 数据 Listener（dataType=data）
 */
@Configuration
@DependsOn("imInitRunner")
public class ImDataListener extends ImAbstractListener {

    @Resource
    private ImKafkaDataBusiness imKafkaDataBusiness;

    @Override
    protected Predicate<ImKafkaListenDataDTO> filter() {
        return dto -> "data".equals(dto.getDataType());
    }

    @Override
    protected CacheDef<List<ImTopicInfoBO>> topicCacheKey() {
        return ImCacheKeysName.DATA_TOPICS;
    }

    @Override
    protected String groupPrefix() {
        return "kafka-data";
    }

    @Override
    protected void consume(List<ImKafkaListenDataDTO> dataList) {
        // 根据策略工厂来消费数据，消息里面有很多类型
        imKafkaDataBusiness.handle(dataList);
    }

    @Bean
    public List<MessageListenerContainer> dataContainers() {
        return createContainers();
    }
}
