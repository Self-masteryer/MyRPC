package com.lcx.rpc.loadbalancer.impl;

import com.lcx.rpc.loadbalancer.LoadBalancer;
import com.lcx.rpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询负载均衡器
 */
public class RoundRobinLoadBalancer implements LoadBalancer {

    // 当前轮询下标map
    private final ConcurrentHashMap<String, AtomicInteger> currentIndexMap = new ConcurrentHashMap<>();

    @Override
    public ServiceMetaInfo select(Map<String, Object> params, List<ServiceMetaInfo> serviceMetaInfos) {
        if (serviceMetaInfos == null || serviceMetaInfos.isEmpty()) return null;

        String serviceName = (String) params.get("serviceName");
        if (serviceName == null) serviceName = serviceMetaInfos.get(0).getName();

        int index = currentIndexMap.computeIfAbsent(serviceName, key -> new AtomicInteger(0))
                .getAndUpdate(i -> (i + 1) % serviceMetaInfos.size());
        return serviceMetaInfos.get(index);
    }

    @Override
    public void refresh(String serviceKey, List<ServiceMetaInfo> serviceMetaInfos) {

    }
}
