package com.report.common.response;

import lombok.Data;

/**
 * 基础响应类
 * 所有API响应的基类，提供通用的状态码和消息字段
 */
@Data
public class BaseResponse {

    /** 成功 */
    public static final Integer SUCCESS = 200;

    /** 失败 */
    public static final Integer FAIL = 400;

    /** 服务忙 */
    public static final Integer ERROR = 500;

    /** 状态码 */
    private Integer status;

    /** 描述信息 */
    private String message;
}
