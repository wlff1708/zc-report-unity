package com.report.module.im.service.impl;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONUtil;
import com.report.common.util.cache.Caches;
import com.report.module.im.constants.ImCacheKeysName;
import com.report.module.im.enums.ImAlarmRecordTypeEnum;
import com.report.module.im.enums.ImAlarmStorageResultEnum;
import com.report.module.im.pojo.bo.*;
import com.report.module.im.service.ImAlarmAllRecorderService;
import com.report.module.im.service.ImAlarmFileStandardStrategy;
import com.report.module.im.service.ImDefaultAlarmFileStandardStrategy;
import com.report.module.im.util.ImAlarmUtil;
import com.report.module.im.util.ImStorageUtil;
import com.report.module.im.util.ImUserAgentUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 告警文件默认流程处理新标准策略实现
 */
@Slf4j
@Component
public class ImDefaultAlarmFileNewStrategy extends ImDefaultAlarmFileStandardStrategy implements ImAlarmFileStandardStrategy {

    @Resource
    private ImAlarmAllRecorderService imAlarmAllRecorderService;

    @Override
    public void dealFile(ImFileDealBO fileDealBO) {
        // 本级处理
        localDeal(fileDealBO);
        // 级联上报
        s2Deal(fileDealBO);
        // 指调上报
        js2Deal(fileDealBO);
    }

    private void js2Deal(ImFileDealBO fileDealBO) {

    }

    private void s2Deal(ImFileDealBO fileDealBO) {
    }

    private void localDeal(ImFileDealBO fileDealBO) {
        // 子模块分组处理
        Map<String, List<ImFileTopicMsgBO>> subModuleFileTopicMsgBOMap = MapUtil.newHashMap();
        // 告警文件记录
        List<ImAlarmRecordBO> fileRecordList = ListUtil.toList();

        fileDealBO.fileTopicMsgBOList().forEach(fileTopicMsgBO -> {
            // 主逻辑：告警文件落盘
            ImAlarmRecordBO fileRecord = storageAlarmFile(fileDealBO.module(), fileTopicMsgBO);
            fileRecordList.add(fileRecord);

            // 顺带做一下：按子模块分组填充，新标准下告警描述要依据子模块进入不同目录落盘
            subModuleFileTopicMsgBOMap
                    .computeIfAbsent(fileTopicMsgBO.getSubModule(), k -> new ArrayList<>())
                    .add(fileTopicMsgBO);
        });

        // 告警描述批量落盘
        List<ImAlarmRecordBO> descRecordList = storageAlarmDesc(fileDealBO.module(), subModuleFileTopicMsgBOMap);

        // 合并文件和描述的记录集合，批量入库
        fileRecordList.addAll(descRecordList);
        imAlarmAllRecorderService.batchSaveRecords(fileRecordList);
    }

    private List<ImAlarmRecordBO> storageAlarmDesc(String module, Map<String, List<ImFileTopicMsgBO>> subModuleFileTopicMsgBOMap) {
        // 获取临时文件路径
        String basePath = Caches.get(ImCacheKeysName.TMP_PATH);
        // 获取S3文件路径
        String s3path = Caches.get(ImCacheKeysName.S3_PATH);
        // 描述记录对象
        List<ImAlarmRecordBO> descRecordList = ListUtil.toList();

        // 构建新落盘内容
        // 遍历子模块
        subModuleFileTopicMsgBOMap.forEach((subModule, imFileTopicMsgBOList) -> {
            // 批量描述处理对象
            List<String> strorageDescList = ListUtil.toList();
            // 当前批次的描述记录对象
            List<ImAlarmRecordBO> currentBatchdescRecordList = ListUtil.toList();

            // 遍历构造
            imFileTopicMsgBOList.forEach(fileTopicMsgBO -> {
                ImAlarmRecordBO descRecord = new ImAlarmRecordBO();
                ImAlarmDescParseBO descParseBO = ImAlarmUtil.parse(fileTopicMsgBO.getFileDesc());
                String deviceId = ImUserAgentUtil.getDeviceId(fileTopicMsgBO.getUserAgent());
                // 先设置已经确定的参数
                descRecord.setDeviceId(deviceId);
                descRecord.setAlarmId(descParseBO.alarmId());
                descRecord.setAlarmTime(DateUtil.parseLocalDateTime(descParseBO.alarmTime()));
                descRecord.setAlarmType(ImAlarmRecordTypeEnum.DESC_FILE.getCode());
                descRecord.setModule(module);
                descRecord.setSubModule(fileTopicMsgBO.getSubModule());
                currentBatchdescRecordList.add(descRecord);
                // 构建落盘描述标准内容
                String content = ImStorageUtil.buildNewStandardDesc(deviceId, descParseBO.metaData());
                strorageDescList.add(content);
            });

            // 开始落盘,同一子模块的描述信息统一落盘
            // 暂存临时目录
            String tmpDescFilePath = ImStorageUtil.buildNewAlarmDescTmpPath(basePath, module, subModule);
            File tmpDescFile = ImStorageUtil.saveFile(tmpDescFilePath, JSONUtil.toJsonStr(strorageDescList));
            // 迁移正式目录
            String s3Dir = Path.of(s3path, module, subModule).toString();
            File s3DescFile = ImStorageUtil.moveFile(tmpDescFile, s3Dir, tmpDescFile.getName());

            // 描述已落盘，继续填充描述记录对象
            currentBatchdescRecordList.forEach(descRecord -> {
                descRecord.setTmpFileName(tmpDescFile.getName());
                descRecord.setS3FileName(s3DescFile.getName());
                descRecord.setFileMd5(SecureUtil.md5(s3DescFile));
                descRecord.setFileSize(ImStorageUtil.calculateFileSize(s3DescFile));
                descRecord.setStorageResult(ImAlarmStorageResultEnum.SUCCESS.getDesc());
            });
            // 填充到描述记录对象中
            descRecordList.addAll(currentBatchdescRecordList);
        });

        return descRecordList;
    }


    private ImAlarmRecordBO storageAlarmFile(String module, ImFileTopicMsgBO fileTopicMsgBO) {
        // 文件记录对象
        ImAlarmRecordBO fileRecord = new ImAlarmRecordBO();

        // 先设置已经确定的参数
        ImAlarmDescParseBO descParseBO = ImAlarmUtil.parse(fileTopicMsgBO.getFileDesc());
        fileRecord.setDeviceId(ImUserAgentUtil.getDeviceId(fileTopicMsgBO.getUserAgent()));
        fileRecord.setAlarmId(descParseBO.alarmId());
        fileRecord.setFileMd5(descParseBO.md5());
        fileRecord.setTmpFileName(fileTopicMsgBO.getSourceFilePath());
        fileRecord.setAlarmTime(DateUtil.parseLocalDateTime(descParseBO.alarmTime()));
        fileRecord.setAlarmType(ImAlarmRecordTypeEnum.SOURCE_FILE.getCode());
        fileRecord.setModule(module);
        fileRecord.setSubModule(fileTopicMsgBO.getSubModule());

        // 开始告警文件落盘操作
        // 重复上传标记
        boolean repeatUpload = descParseBO.upload();
        if (repeatUpload) {
            fileRecord.setStorageResult(ImAlarmStorageResultEnum.REPEATED_UPLOAD.getDesc());
        } else if (StrUtil.isBlank(fileTopicMsgBO.getSourceFilePath()) || !new File(fileTopicMsgBO.getSourceFilePath()).exists()) {
            fileRecord.setStorageResult(ImAlarmStorageResultEnum.FILE_NOT_EXIST.getDesc());
        } else {
            // 告警临时文件存在，迁移到 S3 正式目录
            String s3Dir = Path.of(Caches.get(ImCacheKeysName.S3_PATH), fileTopicMsgBO.getModule(), fileTopicMsgBO.getSubModule()).toString();
            File s3File = ImStorageUtil.moveFile(new File(fileTopicMsgBO.getSourceFilePath()), s3Dir, new File(fileTopicMsgBO.getSourceFilePath()).getName());
            fileRecord.setS3FileName(s3File.getPath());
            fileRecord.setFileMd5(descParseBO.md5());
            fileRecord.setFileSize(ImStorageUtil.calculateFileSize(s3File));
            fileRecord.setStorageResult(ImAlarmStorageResultEnum.SUCCESS.getDesc());
        }
        return fileRecord;
    }

}
