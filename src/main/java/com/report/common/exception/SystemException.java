package com.report.common.exception;

/**
 * 系统异常
 */
public class SystemException extends RuntimeException {

    /**
     * 构造方法
     * @param errorMessage 自定义错误信息
     */
    public SystemException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * 构造方法
     * @param errorMessage 自定义错误信息
     * @param cause 异常原因
     */
    public SystemException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
}
