package com.report.module.im.init;

import com.report.common.util.cache.Caches;
import com.report.module.im.config.ImProperties;
import com.report.module.im.constants.ImCacheKeysName;
import com.report.module.im.constants.ImCommonConstants.SysDict;
import com.report.module.im.pojo.bo.ImTopicInfoBO;
import com.report.module.im.service.ImAlarmUrlModuleService;
import com.report.module.im.service.ImSysDictService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 初始化配置
 */
@Component
public class ImInitRunner {

    @Resource
    private ImAlarmUrlModuleService imAlarmUrlModuleService;

    @Resource
    private ImSysDictService imSysDictService;

    @Resource
    private ImProperties imPathProperties;

    @PostConstruct
    void init() {
        // 缓存设置
        initCache();

        // 检查并创建目录
        initDir();
    }

    private void initDir() {

    }

    private void initCache() {
        // 落盘路径缓存
        Caches.set(ImCacheKeysName.IM_S3_PATH, imPathProperties.getPath().getS3Path());
        Caches.set(ImCacheKeysName.IM_TMP_PATH, imPathProperties.getPath().getTmpPath());
        Caches.set(ImCacheKeysName.IM_S2_PATH, imPathProperties.getPath().getS2Path());
        Caches.set(ImCacheKeysName.IM_JS2_PATH, imPathProperties.getPath().getJs2Path());
        // 落盘标准缓存
        String standard = imSysDictService.getValueByType(SysDict.INTRANET_DATE_TYPE);
        Caches.set(ImCacheKeysName.IM_STORAGE_STANDARD, SysDict.INTRANET_DATE_TYPE_ON.equals(standard));

        // Kafka topic 缓存
        List<ImTopicInfoBO> allTopics = imAlarmUrlModuleService.getAllTopicInfo();
        Caches.set(ImCacheKeysName.IM_DATA_TOPICS, allTopics.stream()
                .filter(t -> SysDict.DATA_TYPE_DATA.equals(t.dataType()))
                .toList());
        Caches.set(ImCacheKeysName.IM_FILE_TOPICS, allTopics.stream()
                .filter(t -> SysDict.DATA_TYPE_FILE.equals(t.dataType()))
                .toList());

    }
}
