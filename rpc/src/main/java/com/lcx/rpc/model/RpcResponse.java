package com.lcx.rpc.model;

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
    private Exception exception;

    /**
     * 构造成功信息
     */
    public static RpcResponse success(Object data) {
        return RpcResponse.builder().code(200).data(data).dataType(data.getClass()).build();
    }

    /**
     * 构造失败信息
     */
    public static RpcResponse fail() {
        return RpcResponse.builder().code(500).message("服务器发生错误").build();
    }
}