package com.report.common.util.kafka;

import com.report.common.exception.ThirdPartyException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * kafka工具类
 */
@Slf4j
@Component
public class KafkaUtil {

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    public void send(String topic, String data) {
        try {
            kafkaTemplate.send(topic, data);
        } catch (Exception e) {
            throw new ThirdPartyException("发送消息失败", e);
        }
    }
}
