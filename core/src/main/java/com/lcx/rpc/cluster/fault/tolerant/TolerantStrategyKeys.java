package com.lcx.rpc.cluster.fault.tolerant;

/**
 * 容错策略常量
 */
public final class TolerantStrategyKeys {

    /**
     * 快速恢复
     */
    public static final String FAIL_FAST = "failFast";

    /**
     * 静默处理
     */
    public static final String FAIL_SAFE = "failSafe";

    /**
     * 故障恢复
     */
    public static final String FAIL_BACK = "failBack";

    /**
     * 故障转移
     */
    public static final String FAIL_OVER = "failOver";

}
