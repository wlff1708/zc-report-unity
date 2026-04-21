package com.report.module.im.listener;

import com.alibaba.fastjson.JSONObject;
import com.report.common.util.cache.CacheDef;
import com.report.common.util.cache.Caches;
import com.report.module.im.pojo.bo.ImTopicInfoBO;
import com.report.module.im.pojo.dto.ImKafkaListenDataDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.BatchAcknowledgingMessageListener;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.Acknowledgment;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 告警 Kafka Listener 抽象基类（模板方法模式）
 * 封装编程式注册 Consumer、消息解析、过滤、异常处理、ack 提交的公共流程。
 * 子类只需提供过滤条件、缓存 key 和业务处理逻辑。
 */
@Slf4j
public abstract class ImAbstractListener {

    @Resource
    private ConsumerFactory<String, String> consumerFactory;

    /**
     * 子类提供数据过滤条件
     */
    protected abstract Predicate<ImKafkaListenDataDTO> filter();

    /**
     * 子类提供 topic 元信息的缓存 key
     */
    protected abstract CacheDef<List<ImTopicInfoBO>> topicCacheKey();

    /**
     * 子类提供 Consumer Group ID 前缀
     */
    protected abstract String groupPrefix();

    /**
     * 子类提供过滤后的业务处理逻辑
     */
    protected abstract void consume(List<ImKafkaListenDataDTO> dataList);

    /**
     * 注册 Consumer 容器
     */
    protected List<MessageListenerContainer> createContainers() {
        List<ImTopicInfoBO> topicInfoBOS = Caches.get(topicCacheKey());
        log.info("注册{} Consumer: {} 个 topic", groupPrefix(), topicInfoBOS.size());
        return topicInfoBOS.stream()
                .map(ti -> createContainer(ti.topic(), ti.partition()))
                .collect(Collectors.toList());
    }

    /**
     * 为单个 topic 创建 Kafka Consumer 容器
     */
    private ConcurrentMessageListenerContainer<String, String> createContainer(String topic, int concurrency) {
        ContainerProperties props = new ContainerProperties(topic);
        props.setGroupId(groupPrefix() + "-" + topic);
        props.setAckMode(ContainerProperties.AckMode.MANUAL);
        props.setMessageListener((BatchAcknowledgingMessageListener<String, String>) this::handle);
        ConcurrentMessageListenerContainer<String, String> container =
                new ConcurrentMessageListenerContainer<>(consumerFactory, props);
        container.setConcurrency(concurrency);
        container.setBeanName(groupPrefix() + "-container-" + topic);
        return container;
    }

    /**
     * 统一消息处理流程：解析 → 过滤 → 业务处理 → ack
     */
    private void handle(List<ConsumerRecord<String, String>> records, Acknowledgment ack) {
        try {
            List<ImKafkaListenDataDTO> dataDTOList = records.stream()
                    .map(r -> JSONObject.parseObject(r.value(), ImKafkaListenDataDTO.class))
                    .filter(dto -> {
                        if (!filter().test(dto)) {
                            log.warn("{}消费跳过不匹配数据，实际dataType={}，uri={}",
                                    groupPrefix(), dto.getDataType(), dto.getUri());
                            return false;
                        }
                        return true;
                    })
                    .toList();

            if (dataDTOList.isEmpty()) {
                log.warn("{}消费数据为空", groupPrefix());
                return;
            }

            log.debug("{}消费数据：{}", groupPrefix(), dataDTOList);
            consume(dataDTOList);
        } catch (Exception e) {
            log.error("{}消息消费失败", groupPrefix(), e);
        } finally {
            ack.acknowledge();
        }
    }
}
