package com.lcx.rpc.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * RPC响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RpcResponse implements Serializable {

    /**
     * 响应码
     */
    private int code;

    /**
     * 响应信息
     */
    private String message;

    /**
     * 响应数据
     */
    private Object data;

    /**
     * 数据类型
     */
    private Class<?> dataType;

    /**
     * 异常信息
     */
    //private Exception exception;
    public static RpcResponse success(Object data) {
        return RpcResponse.builder().code(200).data(data).dataType(data.getClass()).build();
    }

    public static RpcResponse success(Object data, String message) {
        return RpcResponse.builder().code(200).message(message).data(data).dataType(data.getClass()).build();
    }

    public static RpcResponse fail(int code) {
        return RpcResponse.builder().code(code).build();
    }

    public static RpcResponse fail(int code, String message) {
        return RpcResponse.builder().code(code).message(message).build();
    }

    public static RpcResponse retryable() {
        return RpcResponse.builder().code(503).build();
    }

    public static RpcResponse retryable(String message) {
        return RpcResponse.builder().code(503).message(message).build();
    }
}