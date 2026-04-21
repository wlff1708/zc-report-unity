package com.report.module.im.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.report.module.im.pojo.bo.ImAlarmDescParseBO;

/**
 * 告警工具类
 */
public class ImAlarmUtil {

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
        return new ImAlarmDescParseBO(utf8AlarmDescJsonObj.getString("id"),
                utf8AlarmDescJsonObj.getBooleanValue("is_upload"),
                utf8AlarmDescJsonObj.getString("filetype"),
                utf8AlarmDescJsonObj.getString("checksum"),
                uft8AlarmDesc,
                utf8AlarmDescJsonObj.getString("time")

        );
    }
}
