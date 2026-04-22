package com.report.module.im.business;

import com.alibaba.fastjson.JSONObject;
import com.report.BaseTest;
import com.report.common.util.cache.Caches;
import com.report.module.im.config.ImProperties;
import com.report.module.im.constants.ImCacheKeysName;
import com.report.module.im.pojo.dto.ImKafkaListenDataDTO;
import com.report.module.im.service.ImAlarmAllRecorderService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件类 Kafka 业务处理层测试
 */
public class ImKafkaFileBusinessTest extends BaseTest {

    @Resource
    private ImKafkaFileBusiness imKafkaFileBusiness;

    @Resource
    private ImAlarmAllRecorderService imAlarmAllRecorderService;

    @Resource
    private ImProperties imProperties;

    /**
     * 测试新标准下的告警文件处理
     */
    @Test
    @DisplayName("测试新标准下的告警文件处理")
    public void testNewStandard() {
        // 设置新标准
        Caches.set(ImCacheKeysName.STORAGE_STANDARD, true);

        // 构造测试数据
        List<ImKafkaListenDataDTO> dataList = buildAlarmFile();

        // 执行
        imKafkaFileBusiness.handle(dataList);
    }

    private List<ImKafkaListenDataDTO> buildAlarmFile() {
        List<ImKafkaListenDataDTO> dataList = new ArrayList<>();
        String jsonContent = readFileContent("data/new/alarm/malware/data.json");
        JSONObject json = JSONObject.parseObject(jsonContent);
        ImKafkaListenDataDTO dto = new ImKafkaListenDataDTO();
        dto.setUserAgent(json.getString("user_agent"));
        dto.setModuleType("alarm");
        dto.setSubModuleType("malware");
        dto.setDataType("file");
        dto.setFilePath("data/new/alarm/malware/211201010098_65d9d35f52dbf9b0be89dc0d7cc00e49");
        dto.setS2ReportModule(false);
        dto.setJs2ReportModule(false);
        dataList.add(dto);
        return dataList;
    }
}
