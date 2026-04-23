package com.report.module.im.business;

import com.report.BaseTest;
import com.report.BaseTest.*;
import com.report.common.util.cache.Caches;
import com.report.module.im.constants.ImCacheKeysName;
import com.report.module.im.enums.ImAlarmRecordTypeEnum;
import com.report.module.im.enums.ImAlarmStorageResultEnum;
import com.report.module.im.pojo.bo.ImAlarmRecordBO;
import com.report.module.im.pojo.dto.ImKafkaListenDataDTO;
import com.report.module.im.service.ImAlarmAllRecorderService;
import com.report.module.im.util.ImStorageUtil;
import com.report.module.im.util.shell.ImShellUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 告警文件测试
 */
@Slf4j
public class AlarmFileTest extends BaseTest {

    @TempDir
    Path tempDir;

    @Resource
    private ImKafkaFileBusiness imKafkaFileBusiness;

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
    @DisplayName("测试新标准下的告警文件处理")
    public void testNewStandard(String module, String subModule) throws Exception {
        // 新标准：描述文件无后缀，源文件名 deviceId_checksum
        testAlarmFile(module, subModule, true, "data/new/",
                f -> f.contains(module + "_filedesc_"));
    }

    @ParameterizedTest
    @CsvSource({
            "alarm, malware",
            "alarm, trojan"
    })
    @DisplayName("测试老标准下的告警文件处理")
    public void testOldStandard(String module, String subModule) throws Exception {
        // 老标准：描述文件带 .txt 后缀，源文件名 deviceId_alarmId_checksum
        testAlarmFile(module, subModule, false, "data/old/",
                f -> f.contains(module + "_filedesc_") && f.endsWith(".txt"));
    }

    /**
     * 告警文件处理通用测试方法
     *
     * @param module           父模块
     * @param subModule        子模块
     * @param storageStandard  落盘标准开关
     * @param dataDirPrefix    数据源目录前缀（data/new/ 或 data/old/）
     * @param descFileMatcher  描述文件匹配条件
     */
    private void testAlarmFile(String module, String subModule,
                               boolean storageStandard, String dataDirPrefix,
                               Predicate<String> descFileMatcher) throws Exception {
        Caches.set(ImCacheKeysName.STORAGE_STANDARD, storageStandard);

        // 读取测试数据
        String sourceDataDir = dataDirPrefix + module + "/" + subModule;
        AlarmTestBO alarmTestBO = readFileContent(sourceDataDir);

        // 收集 alarmId 并清理旧数据
        for (AlarmDataTestBO data : alarmTestBO.getAlarmDataList()) {
            currentAlarmIds.add(data.getAlarmId());
            imAlarmAllRecorderService.removeByAlarmId(data.getAlarmId());
        }

        // 为每条告警描述构造 DTO，复制源文件到临时目录
        List<ImKafkaListenDataDTO> dataList = buildDTOList(alarmTestBO, module, subModule);

        // 执行业务处理
        imKafkaFileBusiness.handle(dataList);

        // 验证数据库记录
        verifyDbRecords(alarmTestBO);

        // 验证落盘文件
        String s3Path = Caches.get(ImCacheKeysName.S3_PATH);
        String dir = s3Path + "/" + module;
        String findResult = ImShellUtil.execSimple("ls", dir);
        List<String> files = List.of(findResult.split("\n"));
        logFiles(dir, files);

        // 描述文件断言
        assertEquals(1, files.stream().filter(descFileMatcher).count());

        // 源文件断言：is_upload=false 的才落盘
        long expectedSourceCount = alarmTestBO.getAlarmDescList().stream()
                .filter(d -> !d.isUpload()).count();
        String sourceFileNamePattern = buildSourceFileNamePattern(alarmTestBO, storageStandard);
        assertEquals(expectedSourceCount, files.stream()
                .filter(f -> !f.contains("_filedesc_"))
                .filter(f -> f.contains(sourceFileNamePattern))
                .count());
    }

    /**
     * 构造 DTO 列表，每条告警描述对应一个 DTO
     */
    private List<ImKafkaListenDataDTO> buildDTOList(AlarmTestBO alarmTestBO, String module, String subModule) throws Exception {
        List<ImKafkaListenDataDTO> dataList = new ArrayList<>();
        for (AlarmDescTestBO desc : alarmTestBO.getAlarmDescList()) {
            ImKafkaListenDataDTO dto = new ImKafkaListenDataDTO();
            dto.setUserAgent(alarmTestBO.getUserAgent());
            dto.setModuleType(module);
            dto.setSubModuleType(subModule);
            dto.setDataType("file");
            dto.setData(desc.getMetaData());

            // 复制源文件到临时目录
            Path srcPath = Path.of(this.getClass().getClassLoader().getResource(alarmTestBO.getAlarmFilePath()).getPath());
            String fileName = srcPath.getFileName().toString();
            Path tmpFile = tempDir.resolve("source").resolve(fileName);
            Files.createDirectories(tmpFile.getParent());
            Files.copy(srcPath, tmpFile, StandardCopyOption.REPLACE_EXISTING);
            dto.setFilePath(tmpFile.toString());
            dto.setS2ReportModule(false);
            dto.setJs2ReportModule(false);
            dataList.add(dto);
        }
        return dataList;
    }

    /**
     * 验证数据库记录：每条告警应有 1 条源文件记录 + 1 条描述记录
     */
    private void verifyDbRecords(AlarmTestBO alarmTestBO) {
        for (int i = 0; i < alarmTestBO.getAlarmDataList().size(); i++) {
            AlarmDataTestBO data = alarmTestBO.getAlarmDataList().get(i);
            AlarmDescTestBO desc = alarmTestBO.getAlarmDescList().get(i);

            List<ImAlarmRecordBO> records = imAlarmAllRecorderService.listByAlarmId(data.getAlarmId());
            records.forEach(r -> log.info("记录: alarmId={}, alarmType={}, storageResult={}", data.getAlarmId(), r.getAlarmType(), r.getStorageResult()));
            assertEquals(2, records.size());

            // 源文件：根据 is_upload 判断期望结果
            String expectedSourceResult = desc.isUpload()
                    ? ImAlarmStorageResultEnum.REPEATED_UPLOAD.getDesc()
                    : ImAlarmStorageResultEnum.SUCCESS.getDesc();
            assertTrue(records.stream()
                    .filter(r -> ImAlarmRecordTypeEnum.SOURCE_FILE.getCode() == r.getAlarmType())
                    .allMatch(r -> expectedSourceResult.equals(r.getStorageResult())));
            // 描述文件：始终落盘成功
            assertTrue(records.stream()
                    .filter(r -> ImAlarmRecordTypeEnum.DESC_FILE.getCode() == r.getAlarmType())
                    .allMatch(r -> ImAlarmStorageResultEnum.SUCCESS.getDesc().equals(r.getStorageResult())));
        }
    }

    /**
     * 打印落盘文件信息：源文件大小 + 描述文件内容
     */
    private void logFiles(String dir, List<String> files) {
        files.forEach(filename -> {
            log.info("落盘文件: {}", filename);
            // 打印源文件大小
            if (!filename.contains("_filedesc_")) {
                File sourceFile = new File(dir, filename);
                log.info("源文件大小: {} KB", ImStorageUtil.calculateFileSize(sourceFile));
            }
            // 打印描述文件内容
            if (filename.contains("_filedesc_")) {
                try {
                    String descContent = Files.readString(Path.of(dir, filename));
                    log.info("描述文件内容:\n{}", descContent);
                } catch (Exception e) {
                    log.error("读取描述文件失败", e);
                }
            }
        });
    }

    /**
     * 构建源文件名匹配模式
     * 新标准：deviceId_checksum
     * 老标准：deviceId_alarmId_checksum
     */
    private String buildSourceFileNamePattern(AlarmTestBO alarmTestBO, boolean storageStandard) {
        String deviceId = alarmTestBO.getDeviceId();
        String checksum = alarmTestBO.getAlarmDescList().get(0).getMd5();
        if (storageStandard) {
            return deviceId + "_" + checksum;
        }
        String alarmId = alarmTestBO.getAlarmDataList().get(0).getAlarmId();
        return deviceId + "_" + alarmId + "_" + checksum;
    }

}
