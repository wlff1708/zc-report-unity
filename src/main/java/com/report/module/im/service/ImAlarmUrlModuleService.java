package com.report.module.im.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.report.module.im.pojo.bo.ImTopicInfoBO;
import com.report.module.im.pojo.entity.ImAlarmUrlModuleEntity;

import java.util.List;

/**
 * 告警url模块映射 Service接口
 */
public interface ImAlarmUrlModuleService extends IService<ImAlarmUrlModuleEntity> {

    /**
     * 查询所有Kafka topic元信息
     */
    List<ImTopicInfoBO> getAllTopicInfo();
}
