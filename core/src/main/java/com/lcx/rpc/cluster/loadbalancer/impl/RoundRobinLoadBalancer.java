package com.lcx.rpc.cluster.loadbalancer.impl;

import com.lcx.rpc.cluster.loadbalancer.LoadBalancer;
import com.lcx.rpc.common.model.ServiceMetaInfo;

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
    public ServiceMetaInfo select(Map<String, Object> params, List<ServiceMetaInfo> serviceList) {
        if (serviceList == null || serviceList.isEmpty()) return null;
        String serviceName = getServiceKey(params, serviceList);

        int index = currentIndexMap.computeIfAbsent(serviceName, key -> new AtomicInteger(-1))
                .updateAndGet(i -> (i + 1) % serviceList.size());
        return serviceList.get(index);
    }

    @Override
    public boolean syncState() {
        return false;
    }
}
