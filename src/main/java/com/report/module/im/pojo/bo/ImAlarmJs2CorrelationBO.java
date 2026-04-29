package com.report.module.im.pojo.bo;

import lombok.Data;

/**
 * 指调告警消息和告警文件关联业务对象
 */
@Data
public class ImAlarmJs2CorrelationBO {

    /** 主键 */
    private Integer id;

    /** 设备编号 */
    private String deviceId;

    /** 告警id */
    private Integer alarmId;

    /** 数据类型，0告警文件 1告警消息 */
    private Integer dataType;

    /** 告警是由哪个策略id产生的 */
    private Integer ruleId;

    /** 策略等级 */
    private Integer ruleRisk;

    /** 任务编号，默认为0，非指调策略都是0 */
    private Integer taskId;

    /** 源消息，告警文件类型有用，存储告警描述 */
    private String metaData;

    /** 告警父类型 */
    private String alarmModule;

    /** 告警子类型 */
    private String alarmSubModule;

    /** 上报状态，0等待，1已上报 */
    private Integer reportStatus;

    /** 集群节点编号，标识文件所在节点 */
    private String clusterNo;
}