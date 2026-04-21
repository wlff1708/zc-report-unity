package com.report.module.im.pojo.bo;

/**
 * 告警描述解析BO
 * @param alarmId 告警id
 * @param upload 重复上传标志
 * @param fileType 文件类型
 * @param md5 文件md5
 * @param metaData 告警描述原内容
 * @param alarmTime 告警时间
 */
public record ImAlarmDescParseBO(String alarmId, boolean upload, String fileType, String md5, String metaData, String alarmTime) {
}
