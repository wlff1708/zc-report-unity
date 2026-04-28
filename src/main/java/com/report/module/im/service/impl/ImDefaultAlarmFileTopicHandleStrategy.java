package com.report.module.im.service.impl;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.SecureUtil;
import com.report.common.util.cache.Caches;
import com.report.module.im.constants.ImCacheKeysName;
import com.report.module.im.enums.ImAlarmRecordTypeEnum;
import com.report.module.im.enums.ImAlarmStorageResultEnum;
import com.report.module.im.pojo.bo.ImAlarmDescParseBO;
import com.report.module.im.pojo.bo.ImAlarmRecordBO;
import com.report.module.im.pojo.bo.ImFileDealBO;
import com.report.module.im.pojo.bo.ImFileTopicMsgBO;
import com.report.module.im.service.ImAlarmAllRecorderService;
import com.report.module.im.service.ImHandleFileTopicStrategy;
import com.report.module.im.util.ImAlarmUtil;
import com.report.module.im.util.ImStorageUtil;
import com.report.module.im.util.ImUserAgentUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 默认告警文件处理策略
 * <p>
 * 标准流程：本级落盘 → 级联上报 → 指调上报
 */
@Slf4j
@Component
public class ImDefaultAlarmFileTopicHandleStrategy implements ImHandleFileTopicStrategy {

    @Resource
    private ImAlarmAllRecorderService imAlarmAllRecorderService;

    @Override
    public void handle(ImFileDealBO fileDealBO) {
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
        // 获取s2的上报路径
        String s2Path = Caches.get(ImCacheKeysName.S2_PATH);
        // 按照子模块分组，然后上报
        fileDealBO.fileTopicMsgBOList().stream().collect(Collectors.groupingBy(ImFileTopicMsgBO::getSubModule))
                .forEach((subModule, fileTopicMsgBOList) -> {
                    // 构建阶段：逐条构建，失败的单条记录但不影响其他
                    List<MultipartFile> multipartFiles = new ArrayList<>();
                    List<String> descList = new ArrayList<>();
                    fileTopicMsgBOList.forEach(fileTopicMsgBO -> {
                        MultipartFile multipartFile = ImStorageUtil.buildMultipartFile(s2Path, fileTopicMsgBO.getSourceFilePath());
                        multipartFiles.add(multipartFile);
                        descList.add(ImStorageUtil.buildS2ReportDesc(multipartFile.getName(), fileTopicMsgBO.getUserAgent(), fileTopicMsgBO.getFileDesc()));
                    });
                    // 接下来开始发送
        });
    }

    private void localDeal(ImFileDealBO fileDealBO) {
        // 告警文件记录
        List<ImAlarmRecordBO> fileRecordList = ListUtil.toList();

        fileDealBO.fileTopicMsgBOList().forEach(fileTopicMsgBO -> {
            // 解析告警描述，后续文件落盘和描述落盘共用
            ImAlarmDescParseBO descParseBO = ImAlarmUtil.parse(fileTopicMsgBO.getFileDesc());
            // 主逻辑：告警文件落盘
            ImAlarmRecordBO fileRecord = storageAlarmFile(fileDealBO.module(), fileTopicMsgBO, descParseBO);
            fileRecordList.add(fileRecord);
        });

        // 告警描述批量落盘
        List<ImAlarmRecordBO> descRecordList = storageAlarmDesc(fileDealBO.module(), fileDealBO.fileTopicMsgBOList());

        // 合并文件和描述的记录集合，批量入库
        fileRecordList.addAll(descRecordList);
        imAlarmAllRecorderService.batchSaveRecords(fileRecordList);
    }

    private List<ImAlarmRecordBO> storageAlarmDesc(String module, List<ImFileTopicMsgBO> imFileTopicMsgBOList) {
        // 获取临时文件路径
        String basePath = Caches.get(ImCacheKeysName.TMP_PATH);
        // 获取S3文件路径
        String s3path = Caches.get(ImCacheKeysName.S3_PATH);
        // 获取落盘标准
        Boolean storageStandard = Caches.get(ImCacheKeysName.STORAGE_STANDARD);

        // 构建新落盘内容
        // 批量描述处理对象
        List<String> storageDescList = ListUtil.toList();

        // 当前批次的描述记录对象
        List<ImAlarmRecordBO> descRecordList = ListUtil.toList();

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
            descRecordList.add(descRecord);
            // 构建落盘描述标准内容
            String content = ImStorageUtil.buildAlarmDescDependsStandard(storageStandard, fileTopicMsgBO.getUserAgent(), descParseBO.metaData());
            storageDescList.add(content);
        });

        // 开始落盘,描述信息统一落盘
        // 暂存临时目录
        String tmpDescFilePath = ImStorageUtil.buildAlarmDescTmpPathDependsStandard(storageStandard, basePath, module);
        File tmpDescFile = ImStorageUtil.saveFile(tmpDescFilePath, String.join("", storageDescList));
        // 迁移正式目录
        String s3Dir = Path.of(s3path, module).toString();
        File s3DescFile = ImStorageUtil.moveFile(tmpDescFile, s3Dir, tmpDescFile.getName());

        // 描述已落盘，继续填充描述记录对象
        descRecordList.forEach(descRecord -> {
            descRecord.setS3FileName(s3DescFile.getName());
            descRecord.setFileMd5(SecureUtil.md5(s3DescFile));
            descRecord.setTmpFileName(tmpDescFile.getName());
            descRecord.setFileSize(ImStorageUtil.calculateFileSize(s3DescFile));
            descRecord.setStorageResult(ImAlarmStorageResultEnum.SUCCESS.getDesc());
        });
        return descRecordList;
    }


    private ImAlarmRecordBO storageAlarmFile(String module, ImFileTopicMsgBO fileTopicMsgBO, ImAlarmDescParseBO descParseBO) {
        // 文件记录对象
        ImAlarmRecordBO fileRecord = new ImAlarmRecordBO();

        // 先设置已经确定的参数
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
        } else if (!FileUtil.exist(fileTopicMsgBO.getSourceFilePath())) {
            fileRecord.setStorageResult(ImAlarmStorageResultEnum.FILE_NOT_EXIST.getDesc());
        } else {
            // 告警临时文件存在，迁移到 S3 正式目录
            File sourceFile = new File(fileTopicMsgBO.getSourceFilePath());
            String s3Dir = Path.of(Caches.get(ImCacheKeysName.S3_PATH), fileTopicMsgBO.getModule()).toString();
            File s3File = ImStorageUtil.moveFile(sourceFile, s3Dir, sourceFile.getName());
            fileRecord.setS3FileName(s3File.getPath());
            fileRecord.setFileSize(ImStorageUtil.calculateFileSize(s3File));
            fileRecord.setStorageResult(ImAlarmStorageResultEnum.SUCCESS.getDesc());
        }
        return fileRecord;
    }


}
