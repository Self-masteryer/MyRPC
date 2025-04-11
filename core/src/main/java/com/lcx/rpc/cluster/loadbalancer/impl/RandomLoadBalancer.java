package com.lcx.rpc.cluster.loadbalancer.impl;

import com.lcx.rpc.cluster.loadbalancer.LoadBalancer;
import com.lcx.rpc.common.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机负载均衡器
 */
public class RandomLoadBalancer implements LoadBalancer {

    @Override
    public ServiceMetaInfo select(Map<String, Object> params, List<ServiceMetaInfo> serviceList) {
        if (serviceList == null || serviceList.isEmpty()) return null;
        ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
        return serviceList.get(threadLocalRandom.nextInt(serviceList.size()));
    }

}
