package com.lcx.rpc.loadbalancer;

import com.lcx.rpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;

/**
 * 负载均衡器：策略模式
 */
public interface LoadBalancer {

    /**
     * 选择服务调用
     * @param params
     * @param serviceMetaInfoList
     * @return 服务
     */
    ServiceMetaInfo select(Map<String, Object> params, List<ServiceMetaInfo> serviceMetaInfoList);

}
