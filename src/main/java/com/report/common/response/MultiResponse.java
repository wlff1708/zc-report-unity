package com.report.common.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 集合通用响应类
 * 用于返回多条数据的响应，继承BaseResponse
 */
@Getter
@Setter
public class MultiResponse<T> extends BaseResponse {

    /** 数据集合 */
    private List<T> records;


    /**
     * 创建成功的集合响应（自定义消息）
     */
    public static <T> MultiResponse<T> ofSuccess(List<T> data, String message) {
        MultiResponse<T> response = new MultiResponse<>();
        response.setRecords(data);
        response.setStatus(SUCCESS);
        response.setMessage(message);
        return response;
    }


    /**
     * 创建失败的集合响应（自定义消息）
     */
    public static <T> MultiResponse<T> ofFail(String message) {
        MultiResponse<T> response = new MultiResponse<>();
        response.setStatus(FAIL);
        response.setMessage(message);
        return response;
    }

    /**
     * 创建错误的集合响应
     */
    public static <T> MultiResponse<T> ofError() {
        MultiResponse<T> response = new MultiResponse<>();
        response.setStatus(ERROR);
        response.setMessage("服务器繁忙，请稍后重试");
        return response;
    }

}
