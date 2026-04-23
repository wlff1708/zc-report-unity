package com.report.module.im.business;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.report.BaseTest;
import com.report.common.util.cache.Caches;
import com.report.module.im.constants.ImCacheKeysName;
import com.report.module.im.enums.ImAlarmRecordTypeEnum;
import com.report.module.im.enums.ImAlarmStorageResultEnum;
import com.report.module.im.pojo.bo.ImAlarmRecordBO;
import com.report.module.im.pojo.dto.ImKafkaListenDataDTO;
import com.report.module.im.service.ImAlarmAllRecorderService;
import com.report.module.im.util.ImStorageUtil;
import com.report.module.im.util.ImUserAgentUtil;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 告警消息测试
 */
@Slf4j
public class AlarmDataTest extends BaseTest {

    @TempDir
    Path tempDir;

    @Resource
    private ImKafkaDataBusiness imKafkaDataBusiness;

    @Resource
    private ImAlarmAllRecorderService imAlarmAllRecorderService;

    private List<String> currentAlarmIds = new ArrayList<>();

    @BeforeEach
    void setup() {
        Caches.set(ImCacheKeysName.S3_PATH, tempDir.resolve("s3").toString());
        Caches.set(ImCacheKeysName.TMP_PATH, tempDir.resolve("tmp").toString());
        currentAlarmIds.clear();
    }

    @AfterEach
    void teardown() {
        currentAlarmIds.forEach(id -> imAlarmAllRecorderService.removeByAlarmId(id));
    }

    @ParameterizedTest
    @CsvSource({
            "alarm, malware",
            "alarm, trojan"
    })
    @DisplayName("test new standard alarm data handling")
    public void testNewStandard(String module, String subModule) throws Exception {
        // 新标准：数据文件无后缀
        testAlarmData(module, subModule, true, "data/msg/new/",
                f -> f.contains("_data_") && !f.endsWith(".txt"));
    }

    @ParameterizedTest
    @CsvSource({
            "alarm, malware",
            "alarm, trojan"
    })
    @DisplayName("test old standard alarm data handling")
    public void testOldStandard(String module, String subModule) throws Exception {
        // 老标准：数据文件带 .txt 后缀
        testAlarmData(module, subModule, false, "data/msg/old/",
                f -> f.contains("_data_") && f.endsWith(".txt"));
    }

    /**
     * 告警消息处理通用测试方法
     *
     * @param module           父模块
     * @param subModule        子模块
     * @param storageStandard  落盘标准开关
     * @param dataDirPrefix    数据源目录前缀
     * @param dataFileMatcher  数据文件匹配条件
     */
    private void testAlarmData(String module, String subModule,
                               boolean storageStandard, String dataDirPrefix,
                               Predicate<String> dataFileMatcher) throws Exception {
        Caches.set(ImCacheKeysName.STORAGE_STANDARD, storageStandard);

        // 读取测试数据
        String sourceDataDir = dataDirPrefix + module + "/" + subModule;
        AlarmDataTestData testData = readDataFileContent(sourceDataDir);

        // 收集 alarmId 并清理旧数据
        for (AlarmDataItem item : testData.getAlarmDataList()) {
            currentAlarmIds.add(item.getAlarmId());
            imAlarmAllRecorderService.removeByAlarmId(item.getAlarmId());
        }

        // 构造 DTO 列表
        List<ImKafkaListenDataDTO> dataList = buildDTOList(testData, module, subModule);

        // 执行业务处理
        imKafkaDataBusiness.handle(dataList);

        // 验证数据库记录
        verifyDbRecords(testData, storageStandard);

        // 验证落盘文件
        String s3Path = Caches.get(ImCacheKeysName.S3_PATH);
        String dir = s3Path + "/" + module;
        assertTrue(Files.exists(Path.of(dir)), "落盘目录应存在: " + dir);

        List<String> files = Files.list(Path.of(dir))
                .map(p -> p.getFileName().toString())
                .toList();
        files.forEach(f -> log.info("落盘文件: {}", f));

        // 数据文件断言
        assertEquals(1, files.stream().filter(dataFileMatcher).count());

        // 验证文件内容
        files.stream().filter(dataFileMatcher).findFirst().ifPresent(filename -> {
            try {
                String content = Files.readString(Path.of(dir, filename));
                log.info("数据文件内容:\n{}", content);

                // 新标准应包含分隔线 + Device-ID + Type
                if (storageStandard) {
                    assertTrue(content.contains("Device-ID:" + testData.getDeviceId()));
                    assertTrue(content.contains("Type:" + subModule));
                } else {
                    assertTrue(content.contains("User-Agent:" + testData.getUserAgent()));
                    assertTrue(content.contains("Type:" + module + "(" + subModule + ")"));
                }
            } catch (Exception e) {
                log.error("读取数据文件失败", e);
            }
        });
    }

    /**
     * 构造 DTO 列表
     */
    private List<ImKafkaListenDataDTO> buildDTOList(AlarmDataTestData testData, String module, String subModule) {
        List<ImKafkaListenDataDTO> dataList = new ArrayList<>();
        for (AlarmDataItem item : testData.getAlarmDataList()) {
            ImKafkaListenDataDTO dto = new ImKafkaListenDataDTO();
            dto.setUserAgent(testData.getUserAgent());
            dto.setModuleType(module);
            dto.setSubModuleType(subModule);
            dto.setDataType("data");
            dto.setData(item.getMetaData());
            dto.setS2ReportModule(false);
            dto.setJs2ReportModule(false);
            dataList.add(dto);
        }
        return dataList;
    }

    /**
     * 验证数据库记录
     */
    private void verifyDbRecords(AlarmDataTestData testData, boolean storageStandard) {
        for (AlarmDataItem item : testData.getAlarmDataList()) {
            List<ImAlarmRecordBO> records = imAlarmAllRecorderService.listByAlarmId(item.getAlarmId());
            records.forEach(r -> log.info("记录: alarmId={}, alarmType={}, storageResult={}",
                    item.getAlarmId(), r.getAlarmType(), r.getStorageResult()));
            // data 类型只有告警消息记录（ALARM_MESSAGE）
            assertEquals(1, records.size());
            assertTrue(records.stream()
                    .allMatch(r -> ImAlarmRecordTypeEnum.ALARM_MESSAGE.getCode() == r.getAlarmType()));
            assertTrue(records.stream()
                    .allMatch(r -> ImAlarmStorageResultEnum.SUCCESS.getDesc().equals(r.getStorageResult())));
        }
    }

    /**
     * 读取 data 类型的测试数据
     */
    private AlarmDataTestData readDataFileContent(String filePath) {
        String jsonStr = cn.hutool.core.io.FileUtil.readString(
                this.getClass().getClassLoader().getResource(filePath + "/data.json").getPath(),
                java.nio.charset.StandardCharsets.UTF_8);
        JSONObject json = JSONObject.parseObject(jsonStr);

        AlarmDataTestData testData = new AlarmDataTestData();
        testData.setUserAgent(json.getString("user_agent"));
        testData.setDeviceId(ImUserAgentUtil.getDeviceId(json.getString("user_agent")));

        JSONArray alarmDataArr = json.getJSONArray("alarm_data");
        List<AlarmDataItem> dataList = new ArrayList<>();
        for (int i = 0; i < alarmDataArr.size(); i++) {
            JSONObject dataItem = alarmDataArr.getJSONObject(i);
            AlarmDataItem item = new AlarmDataItem();
            item.setAlarmId(dataItem.getString("id"));
            item.setRuleId(dataItem.getString("rule_id"));
            item.setMetaData(dataItem.toJSONString());
            dataList.add(item);
        }
        testData.setAlarmDataList(dataList);
        return testData;
    }

    /**
     * data 类型测试数据
     */
    @Data
    public static class AlarmDataTestData {
        /** 设备UA */
        private String userAgent;
        /** 设备编号 */
        private String deviceId;
        /** 告警数据列表 */
        private List<AlarmDataItem> alarmDataList;
    }

    /**
     * 告警数据项
     */
    @Data
    public static class AlarmDataItem {
        /** 告警ID */
        private String alarmId;
        /** 规则ID */
        private String ruleId;
        /** 源数据 */
        private String metaData;
    }
}
