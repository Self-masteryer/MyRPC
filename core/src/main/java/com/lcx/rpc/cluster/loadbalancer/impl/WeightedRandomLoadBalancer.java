package com.lcx.rpc.cluster.loadbalancer.impl;

import com.lcx.rpc.cluster.loadbalancer.LoadBalancer;
import com.lcx.rpc.common.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 加权随机负载均衡器：线性遍历法
 */
public class WeightedRandomLoadBalancer implements LoadBalancer {

    @Override
    public ServiceMetaInfo select(Map<String, Object> params, List<ServiceMetaInfo> serviceMetaInfos) {
        if (serviceMetaInfos == null || serviceMetaInfos.isEmpty()) return null;

        int random = ThreadLocalRandom.current().nextInt(serviceMetaInfos.size());
        for (ServiceMetaInfo serviceMetaInfo : serviceMetaInfos) {
            if (random < serviceMetaInfo.getWeight()) return serviceMetaInfo;
            random -= serviceMetaInfo.getWeight();
        }
        return serviceMetaInfos.get(0);
    }

    @Override
    public void refresh(String serviceKey, List<ServiceMetaInfo> serviceMetaInfos) {

    }
}
