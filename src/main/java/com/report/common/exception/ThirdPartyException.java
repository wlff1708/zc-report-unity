package com.report.common.exception;

/**
 * 三方中间件异常
 */
public class ThirdPartyException extends RuntimeException {

    /**
     * 构造方法
     * @param errorMessage 自定义错误信息
     */
    public ThirdPartyException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * 构造方法
     * @param errorMessage 自定义错误信息
     * @param cause 异常原因
     */
    public ThirdPartyException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
}
