package com.report.module.im.constants;

import com.report.common.util.cache.CacheDef;
import com.report.module.im.pojo.bo.ImTopicInfoBO;

import java.util.List;

/**
 * IM缓存定义集中声明，每个 CacheDef 对应一个缓存值
 */
public class ImCacheKeysName {

    // ==================== 本地缓存 ====================

    /** 消息类 topic 列表（data_type=data），从 v1_alarm_url_module 查库加载 */
    public static final CacheDef<List<ImTopicInfoBO>> IM_DATA_TOPICS = CacheDef.local("local:topics:data");

    /** 文件类 topic 列表（data_type=file），从 v1_alarm_url_module 查库加载 */
    public static final CacheDef<List<ImTopicInfoBO>> IM_FILE_TOPICS = CacheDef.local("local:topics:file");



    /** 本级落盘文件存储临时路径 */
    public static final CacheDef<String> IM_TMP_PATH = CacheDef.local("local:path:tmp");

    /** 本级落盘文件存储正式路径 */
    public static final CacheDef<String> IM_S3_PATH = CacheDef.local("local:path:s3");

    /** 级联上报文件存储根路径 */
    public static final CacheDef<String> IM_S2_PATH = CacheDef.local("local:path:s2");

    /** 级联上报上级MC地址 */
    public static final CacheDef<String> IM_S2_BASE_URL = CacheDef.local("im:s2:base:url");

    /** 指调上报文件存储根路径 */
    public static final CacheDef<String> IM_JS2_PATH = CacheDef.local("local:path:js2");

    // ==================== 中央缓存 ====================
    /** 落盘标准切换（新/老标准）true=新标准，false=老标准 */
    public static final CacheDef<Boolean> IM_STORAGE_STANDARD = CacheDef.local("cluster:storage:standard");
    /** 本级管理系统的设备UA */
    public static final CacheDef<String> IM_MC_USER_AGENT = CacheDef.cluster("mc:user_agent");
    /** 级联对接cookie */
    public static final CacheDef<String> IM_S2_COOKIE = CacheDef.cluster("s2:cookie");

}
