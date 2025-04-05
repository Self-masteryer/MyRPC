package com.lcx.rpc.cluster.loadbalancer;

import com.lcx.rpc.common.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;

/**
 * 负载均衡器：策略模式
 */
public interface LoadBalancer {

    /**
     * 选择服务调用
     * @param params 参数
     * @param serviceMetaInfos 服务元数据
     * @return 服务
     */
    ServiceMetaInfo select(Map<String, Object> params, List<ServiceMetaInfo> serviceMetaInfos);

    /**
     * 更新状态
     * @param serviceKey 服务键
     * @param serviceMetaInfos 服务元数据
     */
    void refresh(String serviceKey, List<ServiceMetaInfo> serviceMetaInfos);


}
