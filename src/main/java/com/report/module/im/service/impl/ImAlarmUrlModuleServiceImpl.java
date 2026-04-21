package com.report.module.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.report.module.im.mapper.ImAlarmUrlModuleMapper;
import com.report.module.im.pojo.bo.ImTopicInfoBO;
import com.report.module.im.pojo.entity.ImAlarmUrlModuleEntity;
import com.report.module.im.service.ImAlarmUrlModuleService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 告警url模块映射 Service
 */
@Service
public class ImAlarmUrlModuleServiceImpl extends ServiceImpl<ImAlarmUrlModuleMapper, ImAlarmUrlModuleEntity> implements ImAlarmUrlModuleService {

    @Override
    public List<ImTopicInfoBO> getAllTopicInfo() {
        return list().stream()
                .map(e -> new ImTopicInfoBO(
                        e.getKafkaTopicName(),
                        e.getKafkaTopicPartition(),
                        e.getKafkaTopicReplication(),
                        e.getDataType()))
                .toList();
    }
}
