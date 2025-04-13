package com.lcx.rpc.cluster.loadbalancer.impl;

import com.lcx.rpc.cluster.loadbalancer.LoadBalancer;
import com.lcx.rpc.common.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 加权随机负载均衡器：概率累加法
 */
public class WeightedRandomLoadBalancer implements LoadBalancer {

    @Override
    public ServiceMetaInfo select(Map<String, Object> params, List<ServiceMetaInfo> serviceList) {
        if (serviceList == null || serviceList.isEmpty()) return null;

        int totalWeight = getTotalWeight(serviceList);
        int random = ThreadLocalRandom.current().nextInt(totalWeight);
        for (ServiceMetaInfo serviceMetaInfo : serviceList) {
            if (random < serviceMetaInfo.getWeight()) return serviceMetaInfo;
            random -= serviceMetaInfo.getWeight();
        }
        return serviceList.get(0);
    }

    @Override
    public boolean syncState() {
        return false;
    }

    private int getTotalWeight(List<ServiceMetaInfo> serviceMetaInfos) {
        int sum = 0;
        for (ServiceMetaInfo serviceMetaInfo : serviceMetaInfos) {
            sum += serviceMetaInfo.getWeight();
        }
        return sum;
    }
}
