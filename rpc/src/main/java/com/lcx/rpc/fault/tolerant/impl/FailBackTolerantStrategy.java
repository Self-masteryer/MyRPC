package com.lcx.rpc.fault.tolerant.impl;

import com.lcx.rpc.fault.tolerant.TolerantStrategy;
import com.lcx.rpc.model.RpcResponse;

import java.util.Map;

/**
 * 故障恢复-容错策略：服务降级
 */
public class FailBackTolerantStrategy implements TolerantStrategy {

    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        // TODO 自行扩展，获取降级的服务并调用
        return null;
    }
}
