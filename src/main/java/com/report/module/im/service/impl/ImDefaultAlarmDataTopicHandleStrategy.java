package com.report.module.im.service.impl;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.crypto.SecureUtil;
import com.report.common.util.cache.Caches;
import com.report.module.im.constants.ImCacheKeysName;
import com.report.module.im.enums.ImAlarmRecordTypeEnum;
import com.report.module.im.enums.ImAlarmStorageResultEnum;
import com.report.module.im.pojo.bo.ImAlarmRecordBO;
import com.report.module.im.pojo.bo.ImDataDealBO;
import com.report.module.im.service.ImAlarmAllRecorderService;
import com.report.module.im.service.ImHandleDataTopicStrategy;
import com.report.module.im.util.ImAlarmUtil;
import com.report.module.im.util.ImStorageUtil;
import com.report.module.im.util.ImUserAgentUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * 默认告警消息处理策略
 * <p>
 * 标准流程：本级落盘 → 级联上报 → 指调上报
 */
@Slf4j
@Component
public class ImDefaultAlarmDataTopicHandleStrategy implements ImHandleDataTopicStrategy {

    @Resource
    private ImAlarmAllRecorderService imAlarmAllRecorderService;

    @Override
    public void handle(ImDataDealBO dataDealBO) {
        // 本级处理
        localDeal(dataDealBO);
        // 级联上报
        s2Deal(dataDealBO);
        // 指调上报
        js2Deal(dataDealBO);
    }

    private void js2Deal(ImDataDealBO dataDealBO) {
    }

    private void s2Deal(ImDataDealBO dataDealBO) {
    }

    private void localDeal(ImDataDealBO dataDealBO) {
        String s3path = Caches.get(ImCacheKeysName.S3_PATH);
        String basePath = Caches.get(ImCacheKeysName.TMP_PATH);
        Boolean storageStandard = Caches.get(ImCacheKeysName.STORAGE_STANDARD);

        // 批量消息内容
        List<String> storageDataList = ListUtil.toList();
        // 当前批次的记录对象
        List<ImAlarmRecordBO> recordList = ListUtil.toList();

        // 遍历每条告警消息，构造落盘内容和记录对象
        dataDealBO.dataTopicMsgBOList().forEach(msgBO -> {
            // 解析告警数据中的 id 和 time
            String alarmId = ImAlarmUtil.parseAlarmId(msgBO.getData());
            String alarmTime = ImAlarmUtil.parseAlarmTime(msgBO.getData());
            String deviceId = ImUserAgentUtil.getDeviceId(msgBO.getUserAgent());

            ImAlarmRecordBO record = new ImAlarmRecordBO();
            record.setDeviceId(deviceId);
            record.setAlarmId(alarmId);
            record.setAlarmTime(cn.hutool.core.date.DateUtil.parseLocalDateTime(alarmTime));
            record.setAlarmType(ImAlarmRecordTypeEnum.ALARM_MESSAGE.getCode());
            record.setModule(dataDealBO.module());
            record.setSubModule(msgBO.getSubModule());
            recordList.add(record);

            // 构建落盘消息标准内容
            String content = ImStorageUtil.buildAlarmDataDependsStandard(
                    storageStandard, msgBO.getUserAgent(),
                    dataDealBO.module(), msgBO.getSubModule(), msgBO.getData());
            storageDataList.add(content);
        });

        // 暂存临时目录
        String tmpDataFilePath = ImStorageUtil.buildAlarmDataTmpPathDependsStandard(
                storageStandard, basePath, dataDealBO.module());
        // 正式目录
        String s3Dir = Path.of(s3path, dataDealBO.module()).toString();

        File tmpDataFile = ImStorageUtil.saveFile(tmpDataFilePath, String.join("", storageDataList));


        // 获取S3文件
        File s3DataFile = ImStorageUtil.moveFile(tmpDataFile, s3Dir, tmpDataFile.getName());

        // 填充记录对象
        recordList.forEach(record -> {
            record.setTmpFileName(tmpDataFile.getName());
            record.setS3FileName(s3DataFile.getName());
            record.setFileMd5(SecureUtil.md5(s3DataFile));
            record.setFileSize(ImStorageUtil.calculateFileSize(s3DataFile));
            record.setStorageResult(ImAlarmStorageResultEnum.SUCCESS.getDesc());
        });

        // 批量入库
        imAlarmAllRecorderService.batchSaveRecords(recordList);
    }
}
