package com.report.module.im.constants;

/**
 * IM 模块常量集中声明
 */
public class ImDictTypeConstants {

    /** S3落盘格式类型 */
    public static final String INTRANET_DATE_TYPE = "intranet_date_type";

    /** S3落盘新标准 */
    public static final String INTRANET_DATE_TYPE_ON = "on";

    /** S3落盘老标准 */
    public static final String INTRANET_DATE_TYPE_OFF = "off";

    /** 数据类型-消息 */
    public static final String DATA_TYPE_DATA = "data";

    /** 数据类型-文件 */
    public static final String DATA_TYPE_FILE = "file";

    /**
     * 级联上报 HTTP 接口常量域
     */
    public static final class Exchange {
        public static final String HEADER_USER_AGENT = "User-Agent";
        public static final String HEADER_COOKIE_SESSION = "Cookie";
        public static final String HEADER_CONTENT_FILEDESC = "Content-FileDesc";
        public static final String HEADER_ALARM_FILE_COUNT = "Alarm-File-Count";
        public static final String HEADER_ALARM_COUNT = "Alarm-Count";
        public static final String PARAM_MODULE = "module";
        public static final String PARAM_FILE = "file";
    }
}
