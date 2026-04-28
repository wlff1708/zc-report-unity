package com.report.module.im.pojo.vo;

import lombok.Data;

/**
 * 级联上报响应结果
 */
@Data
public class S2ReportResultVo {
    private Integer code;
    private String message;
    private Object data;
}
