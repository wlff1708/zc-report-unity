package com.report.common.exception;

/**
 * 脚本执行异常
 */
public class ExecuteShellException extends RuntimeException {

    /**
     * 构造方法
     * @param errorMessage 自定义错误信息
     */
    public ExecuteShellException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * 构造方法
     * @param errorMessage 自定义错误信息
     * @param cause 异常原因
     */
    public ExecuteShellException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
}
