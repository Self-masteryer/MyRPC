package com.lcx.rpc.fault.circuitBreaker;

import lombok.Getter;
import java.util.concurrent.atomic.AtomicInteger;

public class CircuitBreaker {

    // 当前状态
    @Getter
    private CircuitBreakerState state = CircuitBreakerState.CLOSED;
    private final AtomicInteger failureCount = new AtomicInteger(0); // 失败次数
    private final AtomicInteger successCount = new AtomicInteger(0); // 成功次数
    private final AtomicInteger requestCount = new AtomicInteger(0); // 请求次数

    // 关闭->开启：失败次数阈值
    private final int failureThreshold;
    // 开启->半开启：恢复时间
    private final long retryTimePeriod;
    // 半开启->关闭：成功次数比例
    private final double halfOpenSuccessRate;
    // 上一次失败时间
    private long lastFailureTime = 0;

    public CircuitBreaker(int failureThreshold, double halfOpenSuccessRate, long retryTimePeriod) {
        this.failureThreshold = failureThreshold;
        this.halfOpenSuccessRate = halfOpenSuccessRate;
        this.retryTimePeriod = retryTimePeriod;
    }

    // 查看当前熔断器是否允许请求通过
    public synchronized boolean allowRequest() {
        long currentTime = System.currentTimeMillis();
        switch (state) {
            case OPEN:
                if (currentTime - lastFailureTime > retryTimePeriod) {
                    state = CircuitBreakerState.HALF_OPEN;
                    resetCounts();
                    return true;
                }
                System.out.println("熔断生效!!!!!!!");
                return false;
            case HALF_OPEN:
                requestCount.incrementAndGet();
                return true;
            case CLOSED:
            default:
                return true;
        }
    }

    // 记录成功
    public synchronized void recordSuccess() {
        if (state == CircuitBreakerState.HALF_OPEN) {
            int successCnt = successCount.incrementAndGet();
            if (successCnt >= halfOpenSuccessRate * requestCount.get()) {
                state = CircuitBreakerState.CLOSED; // 切换为关闭状态
                resetCounts();
            }
        } else {
            resetCounts();
        }
    }

    // 记录失败
    public synchronized void recordFailure() {
        int failureCnt = failureCount.incrementAndGet();
        System.out.println("记录失败,次数:" + failureCount);
        lastFailureTime = System.currentTimeMillis();
        if (state == CircuitBreakerState.HALF_OPEN) { // 半打开状态
            state = CircuitBreakerState.OPEN; // 切换为打开状态
            lastFailureTime = System.currentTimeMillis();
        } else if (failureCnt >= failureThreshold) { // 关闭状态，判断失败次数阈值
            state = CircuitBreakerState.OPEN;
        }
    }

    // 重置次数
    private void resetCounts() {
        failureCount.set(0);
        successCount.set(0);
        requestCount.set(0);
    }
}

enum CircuitBreakerState {
    CLOSED,   // 关闭
    OPEN,     // 开启
    HALF_OPEN // 半关闭
}