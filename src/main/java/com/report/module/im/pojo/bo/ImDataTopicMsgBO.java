package com.report.module.im.pojo.bo;

import lombok.Data;

/**
 * 处理数据类Kafka消息的业务对象
 */
@Data
public class ImDataTopicMsgBO {
    /** 设备UA */
    private String userAgent;
    /** 模块类型 */
    private String module;
    /** 子模块类型 */
    private String subModule;
    /** 告警消息数据（JSON字符串） */
    private String data;
    /** 文件存储节点编号 */
    private String nodeNumber;
    /** 是否属于级联上报模块 */
    private Boolean s2ReportModule;
    /** 是否属于指调上报模块 */
    private Boolean js2ReportModule;
}
