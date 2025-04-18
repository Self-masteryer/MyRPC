package com.lcx.rpc.common.model;

import com.lcx.rpc.common.constant.RpcConstant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * RPC请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RpcRequest implements Serializable {
    /**
     * 接口名称
     */
    private String interfaceName;

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
    /**
     * 终止时间：单位毫秒
     */
    private long deadline;

    /**
     * 服务版本
     */
    @Builder.Default
    private String serviceVersion = RpcConstant.DEFAULT_SERVICE_VERSION;
}
