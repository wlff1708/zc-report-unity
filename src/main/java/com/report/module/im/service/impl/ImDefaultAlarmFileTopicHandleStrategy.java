package com.report.module.im.service.impl;

import com.report.common.util.cache.Caches;
import com.report.module.im.constants.ImCacheKeysName;
import com.report.module.im.pojo.bo.ImFileDealBO;
import com.report.module.im.service.ImAlarmFileStandardStrategy;
import com.report.module.im.service.ImHandleFileTopicStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * 默认告警文件处理策略
 * <p>
 * 标准流程：本级落盘 → 级联上报 → 指调上报
 */
@Slf4j
@Component
public class ImDefaultAlarmFileTopicHandleStrategy implements ImHandleFileTopicStrategy {

    @Resource
    private ImDefaultAlarmFileNewStrategy newStrategy;

    @Resource
    private ImDefaultAlarmFileOldStrategy oldStrategy;

    @Override
    public void handle(ImFileDealBO fileDealBO) {
        // 根据新老标准选择策略实例处理数据
        Boolean standard = Caches.get(ImCacheKeysName.STORAGE_STANDARD);
        ImAlarmFileStandardStrategy standardStrategy = standard ? newStrategy : oldStrategy;
        standardStrategy.dealFile(fileDealBO);
    }



}
