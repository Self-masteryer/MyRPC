package com.lcx.rpc.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final int errorCode;

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = 400;
    }

    public BusinessException(String message) {
        super(message);
        this.errorCode = 400;
    }

    public BusinessException(Throwable cause) {
        super(cause);
        this.errorCode = 400;
    }

    public BusinessException(int errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

}
