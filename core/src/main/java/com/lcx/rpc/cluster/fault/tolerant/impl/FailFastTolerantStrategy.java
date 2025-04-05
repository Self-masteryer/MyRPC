package com.lcx.rpc.cluster.fault.tolerant.impl;

import com.lcx.rpc.cluster.fault.tolerant.TolerantStrategy;
import com.lcx.rpc.common.model.RpcResponse;

import java.util.Map;

/**
 * 快速失败-容错策略
 */
public class FailFastTolerantStrategy implements TolerantStrategy {
    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        throw new RuntimeException("服务报错",e);
    }
}
