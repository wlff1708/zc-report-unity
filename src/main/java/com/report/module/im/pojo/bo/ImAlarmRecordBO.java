package com.report.module.im.pojo.bo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 告警全量记录业务对象
 */
@Data
public class ImAlarmRecordBO {

    /** 设备编号 */
    private String deviceId;

    /** 告警编号 */
    private String alarmId;

    /** 文件MD5值 */
    private String fileMd5;

    /** 规则编号 */
    private String ruleId;

    /** 临时文件名称 */
    private String tmpFileName;

    /** S3文件名称 */
    private String s3FileName;

    /** 告警时间 */
    private LocalDateTime alarmTime;

    /** 文件大小 */
    private BigDecimal fileSize;

    /** 告警类型，0源文件，1告警信息，2描述文件 */
    private Integer alarmType;

    /** 告警父模块 */
    private String module;

    /** 子模块 */
    private String subModule;

    /** 告警落盘结果描述 */
    private String storageResult;
}
