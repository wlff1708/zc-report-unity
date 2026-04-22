package com.report.module.im.enums;

import lombok.Getter;

/**
 * 告警记录类型
 */
@Getter
public enum ImAlarmRecordTypeEnum {

    /**
     * 源文件
     */
    SOURCE_FILE(0),
    /**
     * 告警信息
     */
    ALARM_MESSAGE(1),
    /**
     * 描述文件
     */
    DESC_FILE(2);

    private final int code;

    ImAlarmRecordTypeEnum(int code) {
        this.code = code;
    }
}
