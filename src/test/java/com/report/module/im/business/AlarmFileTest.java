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
import com.report.module.im.util.shell.ImShellUtil;
import jakarta.annotation.Resource;
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
        Caches.set(ImCacheKeysName.STORAGE_STANDARD, true);

        String sourceDataDir = "data/new/" + module + "/" + subModule;
        AlarmTestBO alarmTestBO = readFileContent(sourceDataDir);

        // 收集 alarmId 并清理旧数据
        for (AlarmDataTestBO data : alarmTestBO.getAlarmDataList()) {
            currentAlarmIds.add(data.getAlarmId());
            imAlarmAllRecorderService.removeByAlarmId(data.getAlarmId());
        }

        // 为每条告警描述构造 DTO，复制源文件到临时目录
        List<ImKafkaListenDataDTO> dataList = new ArrayList<>();
        for (AlarmDescTestBO desc : alarmTestBO.getAlarmDescList()) {
            ImKafkaListenDataDTO dto = new ImKafkaListenDataDTO();
            dto.setUserAgent(alarmTestBO.getUserAgent());
            dto.setModuleType(module);
            dto.setSubModuleType(subModule);
            dto.setDataType("file");
            dto.setData(desc.getMetaData());

            Path srcPath = Path.of(this.getClass().getClassLoader().getResource(alarmTestBO.getAlarmFilePath()).getPath());
            String fileName = srcPath.getFileName().toString();
            Path tmpFile = tempDir.resolve("source").resolve(desc.getAlarmId() + "_" + fileName);
            Files.createDirectories(tmpFile.getParent());
            Files.copy(srcPath, tmpFile);
            dto.setFilePath(tmpFile.toString());
            dto.setS2ReportModule(false);
            dto.setJs2ReportModule(false);
            dataList.add(dto);
        }

        imKafkaFileBusiness.handle(dataList);

        // 验证数据库记录
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

        // 验证落盘文件
        String s3Path = Caches.get(ImCacheKeysName.S3_PATH);
        String dir = s3Path + "/" + module + "/" + subModule;
        String findResult = ImShellUtil.execSimple("ls", dir);
        List<String> files = List.of(findResult.split("\n"));
        files.forEach(filename -> log.info("落盘文件: {}", filename));

        // 描述文件 1 个（同子模块合并落盘）
        assertEquals(1, files.stream().filter(f -> f.contains(module + "_filedesc_")).count());

        // 源文件：is_upload=false 的才落盘，文件名包含 deviceId_checksum
        long expectedSourceCount = alarmTestBO.getAlarmDescList().stream()
                .filter(d -> !d.isUpload()).count();
        String deviceId = alarmTestBO.getDeviceId();
        String checksum = alarmTestBO.getAlarmDescList().get(0).getMd5();
        assertEquals(expectedSourceCount, files.stream()
                .filter(f -> !f.contains("_filedesc_"))
                .filter(f -> f.contains(deviceId + "_" + checksum))
                .count());
    }

}
