package com.report.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页数据结果类
 * 用于 Business 层返回纯分页数据，不包含 HTTP 响应状态
 *
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /**
     * 当前页码，起始值 1
     */
    private Integer current;

    /**
     * 每页大小
     */
    private Integer size;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Integer pages;

    /**
     * 当前页的数据列表
     */
    private List<T> records;

    /**
     * 计算总页数
     */
    public Integer getPages() {
        if (total == null || size == null || size <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) total / size);
    }

    /**
     * 转换为成功的分页响应
     *
     * @param convertedRecords 转换后的数据列表
     * @param message 响应消息
     * @return 分页响应
     */
    public <R> PageResponse<R> toSuccessResponse(List<R> convertedRecords, String message) {
        return PageResponse.ofSuccess(convertedRecords, total.intValue(), size, current, message);
    }

    /**
     * 转换为成功的分页响应（默认消息）
     *
     * @param convertedRecords 转换后的数据列表
     * @return 分页响应
     */
    public <R> PageResponse<R> toSuccessResponse(List<R> convertedRecords) {
        return toSuccessResponse(convertedRecords, "查询成功");
    }

    /**
     * 从 MyBatis-Plus IPage 创建 PageResult
     *
     * @param records 记录列表
     * @param total 总记录数
     * @param current 当前页
     * @param size 每页大小
     * @return PageResult
     */
    public static <T> PageResult<T> of(List<T> records, long total, long current, long size) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(total);
        result.setCurrent((int) current);
        result.setSize((int) size);
        result.setPages(result.getPages());
        return result;
    }
}
