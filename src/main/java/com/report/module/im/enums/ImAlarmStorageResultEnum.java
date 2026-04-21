package com.report.module.im.enums;

import lombok.Getter;

/**
 * 告警记录类型
 */
@Getter
public enum ImAlarmStorageResultEnum {


    SUCCESS("落盘成功"),

    FILE_NOT_EXIST("落盘失败，文件不存在"),

    REPEATED_UPLOAD("重复上传");

    private final String desc;

    ImAlarmStorageResultEnum(String desc) {
        this.desc = desc;
    }
}