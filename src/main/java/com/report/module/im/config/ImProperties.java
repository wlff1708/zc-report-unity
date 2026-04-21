package com.report.module.im.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 项目自定义配置属性映射
 */
@Data
@Component
@ConfigurationProperties(prefix = "zcenter")
public class ImProperties {
    /**
     * 路径属性
     */
    private ImPathProperties path;


    @Data
    public static class ImPathProperties {
        /**
         * 临时文件存储路径
         */
        private String tmpPath;
        /**
         * 正式落盘文件存储路径
         */
        private String s3Path;
        /**
         * 级联上报存储路径
         */
        private String s2Path;
        /**
         * 指调文件存储路径
         */
        private String js2Path;


    }
}

