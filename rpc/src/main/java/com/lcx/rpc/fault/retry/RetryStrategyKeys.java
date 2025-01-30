package com.lcx.rpc.fault.retry;

/**
 * 重试策略常量
 */
public final class RetryStrategyKeys {

    /**
     * 不重试-重试策略
     */
    public static final String NO = "no";

    /**
     * 固定时间间隔-重试策略
     */
    public static final String FIXED_INTERVAL = "fixedInterval";

}
