package com.report.module.im.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.json.JSONUtil;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 数据落盘工具类，按新老标准生成不同格式的文件内容。
 */
public class ImStorageUtil {

    private ImStorageUtil() {
    }

    // ==================== 公共常量 ====================
    /**
     * 公共内部域
     */
    private static final class CommonStd {
        /**
         * 告警描述文件名中的标识 key
         */
        public static final String DESC_FILE_NAME_KEY = "filedesc";

        /**
         * 告警消息文件名中的标识 key
         */
        public static final String DATA_FILE_NAME_KEY = "data";

        /**
         * 类型标识
         */
        public static final String TYPE = "Type:";

        /**
         * 行分隔符
         */
        static final String LINE_SEP = "\r\n";
    }

    // ==================== 新标准常量 ====================
    /**
     * 新标准内部域
     */
    private static final class NewStd {
        /**
         * 文件内容分隔符
         */
        static final String DELIMITER = "---------------7d81741d1803de---------------";
        /**
         * 文件内容设备编号头
         */
        static final String DEVICE_ID = "Device-ID:";
        /**
         * 文件内容描述头
         */
        static final String CONTENT_FILEDESC = "Content-Filedesc:";
    }

    // ==================== 老标准常量 ====================

    /**
     * 老标准内部域
     */
    private static final class OldStd {
        /**
         * 文件内容UA头
         */
        static final String USER_AGENT = "User-Agent:";
        /** SOH + STX 控制字符，旧标准数据末尾分隔符 */
        static final String TAIL_SEP = "\u0001\u0002";
    }


    /**
     * 写入文件内容
     *
     * @param filePath 文件完整路径（父目录自动创建）
     * @param content  文件内容
     * @return 写入的文件对象
     */
    public static File saveFile(String filePath, String content) {
        File file = FileUtil.file(filePath);
        FileUtil.writeUtf8String(content, file);
        return file;
    }

    /**
     * 移动文件到目标目录并保留原文件名
     *
     * @param src            源文件
     * @param targetDir      目标目录（自动创建）
     * @param targetFilename 目标文件名
     * @return 目标文件对象
     */
    public static File moveFile(File src, String targetDir, String targetFilename) {
        File target = FileUtil.file(targetDir, targetFilename);
        // 确保目标目录存在
        FileUtil.mkParentDirs(target);
        try {
            Files.move(src.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
        return target;
    }

    /**
     * 根据落盘标准开关，选择新/老标准构造告警描述
     *
     * @param storageStandard 落盘标准开关
     * @param userAgent 设备UA字符串，新标准从中提取deviceId，老标准直接拼入头部
     * @param desc      描述信息（JSON格式），新标准会格式化后写入正文
     * @return 按当前标准拼接完成的落盘数据字符串
     */
    public static String buildAlarmDescDependsStandard(boolean storageStandard, String userAgent, String desc) {
        return storageStandard
                ? buildNewStandardDesc(ImUserAgentUtil.getDeviceId(userAgent), desc)
                : buildOldStandardDesc(userAgent, desc);
    }


    /**
     * 新标准格式构造：分隔线 + Device-ID + Content-Filedesc + JSON格式化数据
     *
     * @param deviceId 设备编号
     * @param desc     描述信息，经JSON格式化后作为正文
     * @return 新标准格式的落盘数据
     */
    public static String buildNewStandardDesc(String deviceId, String desc) {
        return NewStd.DELIMITER + CommonStd.LINE_SEP
                + NewStd.DEVICE_ID + deviceId + CommonStd.LINE_SEP
                + NewStd.CONTENT_FILEDESC + CommonStd.LINE_SEP
                + JSONUtil.formatJsonStr(desc)
                + CommonStd.LINE_SEP;
    }

    /**
     * 老标准格式构造：User-Agent头 + 双换行 + 原始数据
     *
     * @param userAgent 设备UA字符串，直接作为头部值
     * @param desc      描述信息，原样写入正文
     * @return 老标准格式的落盘数据
     */
    private static String buildOldStandardDesc(String userAgent, String desc) {
        return OldStd.USER_AGENT + userAgent + CommonStd.LINE_SEP
                + CommonStd.LINE_SEP
                + desc
                + OldStd.TAIL_SEP;
    }


    /**
     * 根据落盘标准开关，构造告警描述临时文件路径
     *
     * @param baseTmpPath 临时目录
     * @param moduleType    父模块类型
     * @return 告警描述临时文件的完整路径
     */
    public static String buildAlarmDescTmpPathDependsStandard(boolean storageStandard, String baseTmpPath, String moduleType) {
        return storageStandard
                ? buildNewAlarmDescTmpPath(baseTmpPath, moduleType)
                : buildOldAlarmDescTmpPath(baseTmpPath, moduleType);
    }

    /**
     * 新标准告警描述临时文件路径：{tmp}/{module}/{module}_filedesc_{random}_{timestamp}
     *
     * @param baseTmpPath    临时目录
     * @param moduleType    父模块类型
     * @return 新标准告警描述临时文件完整路径
     */
    public static String buildNewAlarmDescTmpPath(String baseTmpPath, String moduleType) {
        String dir = Path.of(baseTmpPath, moduleType).toString();
        String filename = String.join("_", moduleType, CommonStd.DESC_FILE_NAME_KEY,
                String.valueOf(ThreadLocalRandom.current().nextInt(10000, 99999)),
                String.valueOf(System.currentTimeMillis()));
        return Path.of(dir, filename).toString();
    }

    /**
     * 老标准告警描述临时文件路径：{tmp}/{module}/{module}_filedesc_{random}_{timestamp}.txt
     *
     * @param baseTmpPath    临时目录
     * @param moduleType    父模块类型
     * @return 老标准告警描述临时文件完整路径（带 .txt 后缀）
     */
    public static String buildOldAlarmDescTmpPath(String baseTmpPath, String moduleType) {
        String dir = Path.of(baseTmpPath, moduleType).toString();
        String filename = String.join("_", moduleType, CommonStd.DESC_FILE_NAME_KEY,
                String.valueOf(ThreadLocalRandom.current().nextInt(10000, 99999)),
                String.valueOf(System.currentTimeMillis())) + ".txt";
        return Path.of(dir, filename).toString();
    }

    /**
     * 根据落盘标准开关，选择新/老标准构造告警消息
     *
     * @param storageStandard 落盘标准开关
     * @param userAgent 设备UA字符串，新标准从中提取deviceId，老标准直接拼入头部
     * @param module    父模块类型
     * @param subModule 子模块类型
     * @param data      告警数据
     * @return 按当前标准拼接完成的落盘数据字符串
     */
    public static String buildAlarmDataDependsStandard(boolean storageStandard, String userAgent, String module, String subModule, String data) {
        return storageStandard
                ? buildNewStandardAlarmData(ImUserAgentUtil.getDeviceId(userAgent), subModule, data)
                : buildOldStandardAlarmData(userAgent, module, subModule, data);
    }

    /**
     * 新标准格式构造：分隔线 + Device-ID + 子模块类型 + JSON格式化数据
     *
     * @param deviceId  设备编号
     * @param subModule 子模块类型
     * @param data      告警数据
     * @return 新标准格式的落盘数据
     */
    private static String buildNewStandardAlarmData(String deviceId, String subModule, String data) {
        return NewStd.DELIMITER + CommonStd.LINE_SEP
                + NewStd.DEVICE_ID + deviceId + CommonStd.LINE_SEP
                + CommonStd.TYPE + subModule + CommonStd.LINE_SEP
                + JSONUtil.formatJsonStr(data)
                + CommonStd.LINE_SEP;
    }

    /**
     * 旧标准格式构造：UserAgent + 模块类型(子模块) + 原始数据
     *
     * @param userAgent 用户代理信息
     * @param module    父模块类型
     * @param subModule 子模块类型
     * @param data      告警数据
     * @return 旧标准格式的落盘数据
     */
    private static String buildOldStandardAlarmData(String userAgent, String module, String subModule, String data) {
        return OldStd.USER_AGENT + userAgent + CommonStd.LINE_SEP
                + CommonStd.TYPE + module + "(" + subModule + ")" + CommonStd.LINE_SEP
                + CommonStd.LINE_SEP
                + data
                + OldStd.TAIL_SEP;
    }


    /**
     * 根据落盘标准开关，构造告警消息临时文件路径
     *
     * @param storageStandard  新标准落盘标准开关
     * @param baseTmpPath  临时目录基础路径
     * @param moduleType   父模块类型
     * @return 告警消息临时文件的完整路径
     */
    public static String buildAlarmDataTmpPathDependsStandard(boolean storageStandard, String baseTmpPath, String moduleType) {
        return storageStandard
                ? buildNewAlarmDataTmpPath(baseTmpPath, moduleType)
                : buildOldAlarmDataTmpPath(baseTmpPath, moduleType);
    }

    /**
     * 旧标准临时文件路径：{TMP_PATH}/{moduleType}/{moduleType}_{DATA_FILE_NAME_KEY}_{random}_{timestamp}.txt
     *
     * @param tmpPathDir 临时目录路径
     * @param moduleType    父模块类型
     * @return 旧标准格式的临时文件完整路径
     */
    private static String buildOldAlarmDataTmpPath(String tmpPathDir, String moduleType) {
        String dir = Path.of(tmpPathDir, moduleType).toString();
        String filename = String.join("_", moduleType, CommonStd.DATA_FILE_NAME_KEY,
                String.valueOf(ThreadLocalRandom.current().nextInt(10000, 99999)),
                String.valueOf(System.currentTimeMillis())) + ".txt";
        return Path.of(dir, filename).toString();
    }

    /**
     * 新标准临时文件路径：{TMP_PATH}/{moduleType}/{moduleType}_{DATA_FILE_NAME_KEY}_{random}_{timestamp}
     * <p>
     * 与旧标准区别：不带 .txt 后缀
     *
     * @param tmpPathDir 临时目录路径
     * @param moduleType    父模块类型
     * @return 新标准格式的临时文件完整路径
     */
    private static String buildNewAlarmDataTmpPath(String tmpPathDir,String moduleType) {
        String dir = Path.of(tmpPathDir, moduleType).toString();
        String filename = String.join("_", moduleType, CommonStd.DATA_FILE_NAME_KEY,
                String.valueOf(ThreadLocalRandom.current().nextInt(10000, 99999)),
                String.valueOf(System.currentTimeMillis()));
        return Path.of(dir, filename).toString();
    }



    /**
     * 计算文件大小
     *  DECIMAL(10, 3) 表示总共10位数字，其中3位小数,最大值：9999999.999 KB（约 9.5 TB），足够使用
     * @param file 文件对象
     * @return 文件大小，单位KB
     */
    public static BigDecimal calculateFileSize(File file) {
        return NumberUtil.round(file.length() / 1024.0, 3);
    }


}