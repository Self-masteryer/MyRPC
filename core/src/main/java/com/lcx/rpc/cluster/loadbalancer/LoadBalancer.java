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
     * @param serviceList 服务列表
     * @return 服务
     */
    ServiceMetaInfo select(Map<String, Object> params, List<ServiceMetaInfo> serviceList);

    /**
     * 获取服务键
     * @param params 参数
     * @param serviceList 服务列表
     * @return 服务键
     */
    default String getServiceKey(Map<String, Object> params, List<ServiceMetaInfo> serviceList) {
        String serviceKey = (String) params.get("serviceKey");
        if (serviceKey == null) {
            serviceKey = serviceList.get(0).getName();
        }
        return serviceKey;
    }

    /**
     * 刷新状态
     * @param serviceKey 服务键
     * @param serviceList 服务列表
     */
    default void refresh(String serviceKey, List<ServiceMetaInfo> serviceList) {

    }

    /**
     * 添加服务
     * @param serviceKey 服务键
     * @param serviceMetaInfo 服务元数据
     */
    default void add(String serviceKey, ServiceMetaInfo serviceMetaInfo) {

    }

    /**
     * 删除服务
     * @param serviceKey 服务键
     * @param serviceMetaInfo 服务元数据
     */
    default void remove(String serviceKey, ServiceMetaInfo serviceMetaInfo) {

    }

    /**
     * 更新服务
     * @param serviceKey 服务键
     * @param oldServiceMetaInfo 旧服务元数据
     * @param newServiceMetaInfo 新服务元数据
     */
    default void update(String serviceKey, ServiceMetaInfo oldServiceMetaInfo, ServiceMetaInfo newServiceMetaInfo) {

    }
}
