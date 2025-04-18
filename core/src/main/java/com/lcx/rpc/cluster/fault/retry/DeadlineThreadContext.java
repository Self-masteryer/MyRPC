package com.lcx.rpc.cluster.fault.retry;

/**
 * 代理执行终止时间线程上下文
 */
public class DeadlineThreadContext {

    private static final ThreadLocal<Long> deadlineThreadLocal = new ThreadLocal<>();

    /**
     * 获取终止时间，若未设置返回0
     * @return deadline
     */
    public static long getDeadline() {
        Long deadline = deadlineThreadLocal.get();
        return deadline == null ? 0 : deadline;
    }

    /**
     * 获取超时时间
     * @return timeout
     */
    public static long getTimeout() {
        long deadline = getDeadline();
        return deadline - System.currentTimeMillis();
    }

    /**
     * 设置deadline
     * @param deadline 终止时间
     */
    public static void setDeadline(final long deadline) {
        deadlineThreadLocal.set(deadline);
    }

    /**
     * 移除deadline，避免内存溢出
     */
    public static void removeDeadline() {
        deadlineThreadLocal.remove();
    }
}
