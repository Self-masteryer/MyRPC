package com.lcx.rpc.cluster.fault.retry;

import com.lcx.rpc.bootstrap.config.RpcApplication;
import com.lcx.rpc.common.spi.SpiLoader;

/**
 * 重试策略工厂
 */
public class RetryStrategyFactory {

    static {
        SpiLoader.load(RetryStrategy.class);
    }

    /**
     * 配置的重试策略
     */
    public static final RetryStrategy retryStrategy = getRetryStrategy(RpcApplication.getRpcConfig().getRetryStrategy());

    /**
     * 获取重试策略
     * @param key 键值
     * @return 重试策略实例
     */
    public static RetryStrategy getRetryStrategy(String key) {
        return SpiLoader.getInstance(RetryStrategy.class, key);
    }

}
