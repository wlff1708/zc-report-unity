package com.report.module.im.listener;

import com.report.common.util.cache.CacheDef;
import com.report.module.im.business.ImKafkaFileBusiness;
import com.report.module.im.constants.ImCacheKeysName;
import com.report.module.im.pojo.bo.ImTopicInfoBO;
import com.report.module.im.pojo.dto.ImKafkaListenDataDTO;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.MessageListenerContainer;

import java.util.List;
import java.util.function.Predicate;

/**
 *文件 Listener（dataType=file）
 */
@Configuration
public class ImFileListener extends ImAbstractListener {

    @Resource
    private ImKafkaFileBusiness imKafkaFileBusiness;

    @Override
    protected Predicate<ImKafkaListenDataDTO> filter() {
        return dto -> "file".equals(dto.getDataType());
    }

    @Override
    protected CacheDef<List<ImTopicInfoBO>> topicCacheKey() {
        return ImCacheKeysName.FILE_TOPICS;
    }

    @Override
    protected String groupPrefix() {
        return "kafka-file";
    }

    @Override
    protected void consume(List<ImKafkaListenDataDTO> dataList) {
        // 根据策略工厂来消费数据，文件消息里面有很多类型
        imKafkaFileBusiness.handle(dataList);
    }

    @Bean
    public List<MessageListenerContainer> alarmFileContainers() {
        return createContainers();
    }
}
