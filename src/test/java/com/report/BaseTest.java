package com.report;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.report.module.im.util.ImUserAgentUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BaseTest {

    public AlarmTestBO readFileContent(String filePath) {
        try {
            String jsonStr = FileUtil.readString(
                    this.getClass().getClassLoader().getResource(filePath + "/data.json").getPath(),
                    StandardCharsets.UTF_8);
            JSONObject json = JSONObject.parseObject(jsonStr);

            AlarmTestBO bo = new AlarmTestBO();
            bo.setUserAgent(json.getString("user_agent"));
            bo.setDeviceId(ImUserAgentUtil.getDeviceId(json.getString("user_agent")));

            // 解析多条告警数据
            JSONArray alarmDataArr = json.getJSONArray("alarm_data");
            List<AlarmDataTestBO> dataList = new ArrayList<>();
            for (int i = 0; i < alarmDataArr.size(); i++) {
                JSONObject dataItem = alarmDataArr.getJSONObject(i);
                AlarmDataTestBO dataBO = new AlarmDataTestBO();
                dataBO.setAlarmId(dataItem.getString("id"));
                dataBO.setRuleId(dataItem.getString("rule_id"));
                dataBO.setMetaData(dataItem.toJSONString());
                dataList.add(dataBO);
            }
            bo.setAlarmDataList(dataList);

            // 解析多条告警描述（兼容对象和数组两种格式）
            List<AlarmDescTestBO> descList = new ArrayList<>();
            Object alarmDescObj = json.get("alarm_desc");
            if (alarmDescObj instanceof JSONArray) {
                JSONArray alarmDescArr = (JSONArray) alarmDescObj;
                for (int i = 0; i < alarmDescArr.size(); i++) {
                    descList.add(parseDescItem(alarmDescArr.getJSONObject(i)));
                }
            } else if (alarmDescObj instanceof JSONObject) {
                descList.add(parseDescItem((JSONObject) alarmDescObj));
            }
            bo.setAlarmDescList(descList);

            // 告警文件路径
            String alarmFile = json.getString("alarm_file");
            bo.setAlarmFilePath(filePath + "/" + alarmFile);

            return bo;
        } catch (IORuntimeException e) {
            throw new RuntimeException(String.format("文件不存在: %s", filePath));
        }
    }

    private AlarmDescTestBO parseDescItem(JSONObject descItem) {
        AlarmDescTestBO descBO = new AlarmDescTestBO();
        descBO.setAlarmId(descItem.getString("id"));
        descBO.setMd5(descItem.getString("checksum"));
        descBO.setUpload(descItem.getBooleanValue("is_upload"));
        descBO.setMetaData(descItem.toJSONString());
        return descBO;
    }

    @Data
    public static class AlarmTestBO {
        /** 设备UA */
        private String userAgent;
        /** 设备编号 */
        private String deviceId;
        /** 告警数据列表 */
        private List<AlarmDataTestBO> alarmDataList;
        /** 告警描述列表 */
        private List<AlarmDescTestBO> alarmDescList;
        /** 告警文件相对路径 */
        private String alarmFilePath;
    }

    @Data
    public static class AlarmDataTestBO {
        /** 告警ID */
        private String alarmId;
        /** 规则ID */
        private String ruleId;
        /** 源数据 */
        private String metaData;
    }

    @Data
    public static class AlarmDescTestBO {
        /** 告警ID */
        private String alarmId;
        /** 源文件md5 */
        private String md5;
        /** 是否重复上传 */
        private boolean upload;
        /** 源数据 */
        private String metaData;
    }
}
