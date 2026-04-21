package com.report.module.im.init;

import com.report.common.util.cache.Caches;
import com.report.module.im.config.ImProperties;
import com.report.module.im.constants.ImCacheKeysName;
import com.report.module.im.service.ImAlarmUrlModuleService;
import jakarta.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 初始化配置
 */
@Component
public class ImInitRunner implements CommandLineRunner {

    @Resource
    private ImAlarmUrlModuleService imAlarmUrlModuleService;

    @Resource
    private ImProperties imPathProperties;

    @Override
    public void run(String... args) {
        // 缓存设置
        initCache();

        // 检查并创建目录
        initDir();
    }

    private void initDir() {

    }

    private void initCache() {
        // 落盘路径缓存
        Caches.set(ImCacheKeysName.S3_PATH, imPathProperties.getPath().getS3Path());
        Caches.set(ImCacheKeysName.TMP_PATH, imPathProperties.getPath().getTmpPath());
        Caches.set(ImCacheKeysName.S2_PATH, imPathProperties.getPath().getS2Path());
        Caches.set(ImCacheKeysName.JS2_PATH, imPathProperties.getPath().getJs2Path());
        // 落盘标准缓存
        // Kafka topic 缓存

    }
}
