package com.report.module.preload.active.detail;

import lombok.Getter;

/**
 *
 * 产品形态枚举
 */
@Getter
public enum ProductType {

    /** 接入口检测器管理系统，只创建 master 库 */
    IM("im"),

    /** 出口保密监测器管理系统，只创建 slave 库 */
    BM("bm"),

    /** 全量版本，创建 master 和 slave 库 */
    ALL("all");

    private final String code;

    ProductType(String code) {
        this.code = code;
    }

    /**
     * 根据产品形态判断是否需要创建 master 库
     * @param productType 产品形态
     * @return true=需要创建
     */
    public static boolean shouldCreateMaster(ProductType productType) {
        return productType == IM || productType == ALL;
    }

    /**
     * 根据产品形态判断是否需要创建 slave 库
     * @param productType 产品形态
     * @return true=需要创建
     */
    public static boolean shouldCreateSlave(ProductType productType) {
        return productType == BM || productType == ALL;
    }

    /**
     * 从产品类型代码解析枚举
     * @param code 产品类型代码：im, bm, all
     * @return 产品形态
     */
    public static ProductType fromCode(String code) {
        return switch (code) {
            case "im" -> IM;
            case "bm" -> BM;
            case "all" -> ALL;
            default -> throw new IllegalArgumentException("未知产品形态: " + code + "，有效值: im, bm, all");
        };
    }
}
