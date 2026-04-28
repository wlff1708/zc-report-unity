package com.report.module.im.constants;

/**
 * IM 模块通用常量集中声明
 */
public class ImCommonConstants {

    /**
     * 系统字典常量域
     */
    public static final class SysDict {
        /** S3落盘格式类型（字典查询 key） */
        public static final String INTRANET_DATE_TYPE = "intranet_date_type";
        /** S3落盘新标准标识 */
        public static final String INTRANET_DATE_TYPE_ON = "on";
        /** S3落盘老标准标识 */
        public static final String INTRANET_DATE_TYPE_OFF = "off";
        /** 数据类型-消息 */
        public static final String DATA_TYPE_DATA = "data";
        /** 数据类型-文件 */
        public static final String DATA_TYPE_FILE = "file";
    }

    /**
     * 级联上报 HTTP 接口常量域
     */
    public static final class S2Report {
        /** 请求头：设备标识 */
        public static final String HEADER_USER_AGENT = "User-Agent";
        /** 请求头：SESSION Cookie */
        public static final String HEADER_COOKIE_SESSION = "Cookie";
        /** 请求头：告警文件描述 JSON */
        public static final String HEADER_CONTENT_FILEDESC = "Content-FileDesc";
        /** 请求头：告警文件数量统计 JSON */
        public static final String HEADER_ALARM_FILE_COUNT = "Alarm-File-Count";
        /** 请求头：告警消息数量统计 JSON */
        public static final String HEADER_ALARM_COUNT = "Alarm-Count";
        /** 请求参数：告警模块 */
        public static final String PARAM_MODULE = "module";
        /** 请求参数：上传文件 */
        public static final String PARAM_FILE = "file";
    }
}
