package com.report.module.im.util;

/**
 * 设备UA处理工具类
 */
public class ImUserAgentUtil {

    private ImUserAgentUtil() {
    }

    /**
     * 获取设备编号
     *
     * @param userAgent 设备UA
     * @return 设备编号
     */
    public static String getDeviceId(String userAgent) {
        return userAgent.split("/")[0];
    }
}
