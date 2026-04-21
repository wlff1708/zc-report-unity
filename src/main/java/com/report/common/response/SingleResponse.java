package com.report.common.response;

import lombok.Getter;
import lombok.Setter;

/**
 * 单数据通用响应类
 * 用于单条数据的响应，继承BaseResponse
 */
@Getter
@Setter
public class SingleResponse<T> extends BaseResponse {

    /** 单个数据 */
    private T record;


    /**
     * 创建成功的单数据响应（自定义消息）
     */
    public static <T> SingleResponse<T> ofSuccess(T data, String message) {
        SingleResponse<T> response = new SingleResponse<>();
        response.setRecord(data);
        response.setStatus(SUCCESS);
        response.setMessage(message);
        return response;
    }

    /**
     * 创建失败的单数据响应（自定义消息）
     */
    public static <T> SingleResponse<T> ofFail(String message) {
        SingleResponse<T> response = new SingleResponse<>();
        response.setStatus(FAIL);
        response.setMessage(message);
        return response;
    }

    /**
     * 创建错误的单数据响应
     */
    public static <T> SingleResponse<T> ofServerError() {
        SingleResponse<T> response = new SingleResponse<>();
        response.setStatus(ERROR);
        response.setMessage("服务器繁忙，请稍后重试");
        return response;
    }
}
