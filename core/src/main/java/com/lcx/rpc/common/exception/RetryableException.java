package com.lcx.rpc.common.exception;

import lombok.Getter;

/**
 * 可重试异常：抛出此异常，客户端会进行重试
 */
@Getter
public class RetryableException extends RuntimeException {

    private final int errorCode;

    public RetryableException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = 503;
    }

    public RetryableException(String message) {
        super(message);
        this.errorCode = 503;
    }

    public RetryableException(Throwable cause) {
        super(cause);
        this.errorCode = 503;
    }

    public RetryableException(int errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

}
