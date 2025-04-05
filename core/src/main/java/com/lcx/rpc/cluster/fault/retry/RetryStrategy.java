package com.lcx.rpc.cluster.fault.retry;

import com.lcx.rpc.common.model.RpcResponse;

import java.util.concurrent.Callable;

/**
 * 重试策略
 */
@FunctionalInterface
public interface RetryStrategy {

    /**
     * 以重试方式执行回调任务
     * @param callable 回调接口
     * @return Rpc响应
     */
    RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception;

}
