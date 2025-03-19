package com.lcx.rpc.loadbalancer.impl;

import com.lcx.rpc.loadbalancer.LoadBalancer;
import com.lcx.rpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机负载均衡器
 */
public class RandomLoadBalancer implements LoadBalancer {

    @Override
    public ServiceMetaInfo select(Map<String, Object> params, List<ServiceMetaInfo> serviceMetaInfos) {
        if (serviceMetaInfos == null || serviceMetaInfos.isEmpty()) return null;
        return serviceMetaInfos.get(ThreadLocalRandom.current().nextInt(serviceMetaInfos.size()));
    }

    @Override
    public void refresh(String serviceKey, List<ServiceMetaInfo> serviceMetaInfos) {

    }
}
