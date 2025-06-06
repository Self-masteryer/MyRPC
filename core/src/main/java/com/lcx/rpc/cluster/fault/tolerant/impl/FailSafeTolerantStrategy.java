package com.lcx.rpc.cluster.fault.tolerant.impl;

import com.lcx.rpc.cluster.fault.tolerant.TolerantStrategy;
import com.lcx.rpc.common.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 静默处理-容错策略
 */
@Slf4j
public class FailSafeTolerantStrategy implements TolerantStrategy {
    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        log.info("静默处理异常", e);
        return RpcResponse.fail(500);
    }
}
