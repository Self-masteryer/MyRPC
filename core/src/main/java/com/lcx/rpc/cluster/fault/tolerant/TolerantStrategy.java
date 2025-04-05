package com.lcx.rpc.cluster.fault.tolerant;

import com.lcx.rpc.common.model.RpcResponse;

import java.util.Map;

/**
 * 容错策略
 */
@FunctionalInterface
public interface TolerantStrategy {

    /**
     * 容错
     * @param context 上下文
     * @param e 异常
     * @return Rpc响应
     */
    RpcResponse doTolerant(Map<String,Object> context, Exception e);

}
