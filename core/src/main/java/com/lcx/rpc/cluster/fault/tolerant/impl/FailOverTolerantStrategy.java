package com.lcx.rpc.cluster.fault.tolerant.impl;

import com.lcx.rpc.cluster.fault.tolerant.TolerantStrategy;
import com.lcx.rpc.common.model.RpcResponse;

import java.util.Map;

/**
 * 故障转移-容错策略
 */
public class FailOverTolerantStrategy implements TolerantStrategy {
    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        // TODO 自行扩展，获取转移的服务并调用
        return null;
    }
}
