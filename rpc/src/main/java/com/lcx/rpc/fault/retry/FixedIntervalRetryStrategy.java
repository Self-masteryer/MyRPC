package com.lcx.rpc.fault.retry;

import com.github.rholder.retry.*;
import com.lcx.rpc.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 固定时间间隔-重试策略
 */
@Slf4j
public class FixedIntervalRetryStrategy implements RetryStrategy{
    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        // 构建一个重试器，用于在指定条件下自动重试callable
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                // 设置重试条件为Exception类型，即只要执行过程中抛出Exception就尝试重试
                .retryIfExceptionOfType(Exception.class)
                // 设置等待策略为每次重试间隔3秒
                .withWaitStrategy(WaitStrategies.fixedWait(3L, TimeUnit.SECONDS))
                // 设置停止策略为最多尝试3次后停止重试
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                // 完成重试器的构建
                .build();
        // 使用构建好的重试器来执行callable，自动处理重试逻辑
        return retryer.call(callable);
    }
}
