package com.lcx.rpc.loadbalancer;

import com.lcx.rpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询负载均衡器
 */
public class RoundRobinLoadBalancer implements LoadBalancer {

    // 当前轮询下标
    private final AtomicInteger currentIndex = new AtomicInteger(0);

    @Override
    public ServiceMetaInfo select(Map<String, Object> params, List<ServiceMetaInfo> serviceMetaInfoList) {
        if(serviceMetaInfoList == null || serviceMetaInfoList.isEmpty()) return null;
        int index = currentIndex.getAndIncrement() % serviceMetaInfoList.size() ;
        return serviceMetaInfoList.get(index);
    }
}
