package com.report.common.exception;

/**
 * 参数校验异常
 */
public class ArgumentsValidateException extends RuntimeException {

    /**
     * 构造方法
     * @param errorMessage 自定义错误信息
     */
    public ArgumentsValidateException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * 构造方法
     * @param errorMessage 自定义错误信息
     * @param cause 异常原因
     */
    public ArgumentsValidateException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
}
