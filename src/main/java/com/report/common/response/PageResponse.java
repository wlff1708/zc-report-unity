package com.report.common.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 分页数据通用响应类
 * 用于分页数据的响应，继承BaseResponse
 */
@Getter
@Setter
public class PageResponse<T> extends BaseResponse {

    /**
     * 分页信息
     */
    private PageInfo<T> data;

    @Data
    public static class PageInfo<T> {

        /**
         * 当前页码，起始值1
         */
        private int current;

        /**
         * 每页大小
         */
        private int size;

        /**
         * 总页数
         */
        private int pages;

        /**
         * 总记录数
         */
        private int total;

        /**
         * 当前页的数据列表
         */
        private List<T> records;
    }

    /**
     * 创建成功的分页响应（自定义消息）
     */
    public static <T> PageResponse<T> ofSuccess(List<T> data, int total, int pageSize, int currentPage, String message) {
        PageResponse<T> pageResponse = new PageResponse<>();
        PageInfo<T> pageInfo = new PageInfo<>();
        pageInfo.setTotal(total);
        pageInfo.setSize(pageSize);
        pageInfo.setCurrent(currentPage);
        pageInfo.setPages((total + pageSize - 1) / pageSize);
        pageInfo.setRecords(data);
        pageResponse.setData(pageInfo);
        pageResponse.setStatus(SUCCESS);
        pageResponse.setMessage(message);
        return pageResponse;
    }

    /**
     * 创建失败的分页响应（自定义消息）
     */
    public static <T> PageResponse<T> ofFail(String message) {
        PageResponse<T> pageResponse = new PageResponse<>();
        pageResponse.setStatus(FAIL);
        pageResponse.setMessage(message);
        return pageResponse;
    }

    /**
     * 创建错误的分页响应
     */
    public static <T> PageResponse<T> ofServerError() {
        PageResponse<T> pageResponse = new PageResponse<>();
        pageResponse.setStatus(ERROR);
        pageResponse.setMessage("服务器繁忙，请稍后重试");
        return pageResponse;
    }
}
