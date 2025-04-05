package com.lcx.rpc.cluster.fault.retry.impl;

import com.github.rholder.retry.*;
import com.lcx.rpc.cluster.fault.retry.RetryStrategy;
import com.lcx.rpc.common.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 固定时间间隔-重试策略
 */
@Slf4j
public class FixedIntervalRetryStrategy implements RetryStrategy {
    /**
     * 要么正常要么抛出异常
     */
    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        // 构建一个重试器，用于在指定条件下自动重试callable
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                // 所有异常：例如网络异常
                .retryIfException()
                // 响应码为500：服务执行错误
                .retryIfResult(response -> {
                    System.out.println(response.getCode() == 500);
                    return response.getCode() == 500;
                })
                // 等待策略：每次重试间隔3秒
                .withWaitStrategy(WaitStrategies.fixedWait(3L, TimeUnit.SECONDS))
                // 停止策略：最多尝试3次后停止重试
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                // 添加重试监听器
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        log.debug("RetryListener: 第" + attempt.getAttemptNumber() + "次调用");
                    }
                })
                // 完成重试器的构建
                .build();

        // 使用构建好的重试器来执行callable，自动处理重试逻辑
        return retryer.call(callable);
//        try {
//
//        } catch (RetryException e) { // 重试次数耗尽，抛出 RetryException
//            Throwable lastException = e.getLastFailedAttempt().getExceptionCause();
//            System.out.println("所有重试失败，最后一次异常：" + lastException);
//        } catch (ExecutionException e) {// 未被重试策略覆盖的异常
//            Throwable rootCause = e.getCause();
//            System.out.println("执行过程中未重试的异常：" + rootCause);
//        }
    }
}