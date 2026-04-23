package com.report.module.im.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.report.module.im.pojo.bo.ImAlarmDescParseBO;

/**
 * 告警工具类
 */
public class ImAlarmUtil {

    private static final String KEY_ID = "id";
    private static final String KEY_TIME = "time";

    private ImAlarmUtil() {
    }

    /**
     * 解析告警描述
     *
     * @param uft8AlarmDesc 告警描述
     * @return 解析结果
     */
    public static ImAlarmDescParseBO parse(String uft8AlarmDesc) {
        JSONObject utf8AlarmDescJsonObj = JSON.parseObject(uft8AlarmDesc);
        return new ImAlarmDescParseBO(utf8AlarmDescJsonObj.getString(KEY_ID),
                utf8AlarmDescJsonObj.getBooleanValue("is_upload"),
                utf8AlarmDescJsonObj.getString("filetype"),
                utf8AlarmDescJsonObj.getString("checksum"),
                uft8AlarmDesc,
                utf8AlarmDescJsonObj.getString(KEY_TIME)

        );
    }

    /**
     * 从告警消息 JSON 中提取告警ID
     *
     * @param alarmData 告警消息 JSON 字符串
     * @return 告警ID
     */
    public static String parseAlarmId(String alarmData) {
        return JSON.parseObject(alarmData).getString(KEY_ID);
    }

    /**
     * 从告警消息 JSON 中提取告警时间
     *
     * @param alarmData 告警消息 JSON 字符串
     * @return 告警时间
     */
    public static String parseAlarmTime(String alarmData) {
        return JSON.parseObject(alarmData).getString(KEY_TIME);
    }
}
