package com.report.module.im.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 告警原始url和标准化模块关系映射表
 */
@Data
@TableName("v1_alarm_url_module")
public class ImAlarmUrlModuleEntity {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /** v1原始uri */
    private String alarmUri;

    /** 标准父模块 */
    private String alarmModule;

    /** 标准子模块 */
    private String alarmSubModule;

    /** 当前模块是否属于级联上报标准模块，0不上报，1要上报 */
    private Integer s2Report;

    /** 当前模块是否属于指调上报标准模块，0不上报，1要上报 */
    private Integer js2Report;

    /** Kafka的topic */
    private String kafkaTopicName;

    /** topic的分区数 */
    private Integer kafkaTopicPartition;

    /** topic的副本数 */
    private Integer kafkaTopicReplication;

    /** 数据类型，两种: data和file */
    private String dataType;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
