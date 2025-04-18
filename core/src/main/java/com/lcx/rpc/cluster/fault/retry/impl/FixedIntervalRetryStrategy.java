package com.lcx.rpc.cluster.fault.retry.impl;

import com.github.rholder.retry.*;
import com.lcx.rpc.cluster.fault.retry.DeadlineThreadContext;
import com.lcx.rpc.cluster.fault.retry.RetryStrategy;
import com.lcx.rpc.common.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 固定时间间隔-重试策略
 */
@Slf4j
public class FixedIntervalRetryStrategy implements RetryStrategy {

    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) {
        long timeout = DeadlineThreadContext.getTimeout();
        if (timeout <= 0) { // 执行超时
            RpcResponse.fail(503, "执行超时");
        }

        // 构建一个重试器，用于在指定条件下自动重试callable
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                // 重试异常：网络异常和超时异常（其他异常将直接抛出）
                .retryIfException(e -> {
                    Throwable current = e;
                    while (current != null) {
                        if (current instanceof IOException || current instanceof TimeoutException) {
                            return true;
                        }
                        current = current.getCause();
                    }
                    return false;
                })
                // 重试结果：响应码503
                .retryIfResult(response -> response != null && response.getCode() == 503)
                // 停止策略：总调用时间不超过 timeout
                .withStopStrategy(StopStrategies.stopAfterDelay(timeout, TimeUnit.MILLISECONDS))
                // 等待策略：指数退避策略：初始0.5秒，最大4秒
                .withWaitStrategy(WaitStrategies.exponentialWait(500, 4, TimeUnit.SECONDS))
                // 添加重试监听器
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        log.debug("RetryListener: 第" + attempt.getAttemptNumber() + "次调用");
                    }
                })
                // 完成重试器的构建
                .build();

        try {
            // 使用构建好的重试器来执行callable，自动处理重试逻辑
            return retryer.call(callable); // 可能成功也可能失败
        } catch (ExecutionException | RetryException e) { // 其他异常和重试异常
            throw new RuntimeException(e);
        }
    }
}