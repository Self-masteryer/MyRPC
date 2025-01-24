package com.lcx.extend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RPC请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RpcRequest {
    /**
     * 接口名称
     */
    private String serviceName;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 参数类型
     */
    private Class<?>[] parameterTypes;

    /**
     * 方法参数
     */
    private Object[] args;
}
