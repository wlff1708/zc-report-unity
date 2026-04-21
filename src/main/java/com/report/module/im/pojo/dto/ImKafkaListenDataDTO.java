package com.report.module.im.pojo.dto;


import lombok.Data;

import java.io.Serializable;

/**
 * Kafka 消息标准信封
 * 数据接入服务 接收设备数据后封装为此 DTO 发送到 Kafka，
 * 数据处理服务（当前服务）从 Kafka 消费。
 */
@Data
public class ImKafkaListenDataDTO implements Serializable {

    /**
     * 设备UA(设备编号/版本号)
     */
    private String userAgent;

    /**
     * 大类：alarm / status / audit / asset
     */
    private String moduleType;

    /**
     * 子类：sensitive / trojan / business_status 等
     */
    private String subModuleType;

    /**
     * 消息类型：data or file
     */
    private String dataType;

    /**
     * 文件路径（告警文件类消息）
     */
    private String filePath;

    /**
     * 文件存储节点编号
     */
    private String storageNode;

    /**
     * 原始请求 URI
     */
    private String uri;

    /**
     * 请求时间戳（ms）
     */
    private String requestTime;

    /**
     * 原始数据（JSON 字符串）
     */
    private String data;

    /**
     * 是否属于级联上报模块
     */
    private Boolean s2ReportModule;

    /**
     * 是否属于指调上报模块
     */
    private Boolean js2ReportModule;

    /**
     * 扩展数据（预留，默认为null）
     */
    private String extend;

}
