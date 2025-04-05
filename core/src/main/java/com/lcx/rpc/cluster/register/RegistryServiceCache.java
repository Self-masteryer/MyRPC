package com.lcx.rpc.cluster.register;


import com.lcx.rpc.common.model.ServiceMetaInfo;

import java.util.List;

/**
 * 注册中心服务本地缓存
 */
public class RegistryServiceCache {
    // 服务缓存
    List<ServiceMetaInfo> serviceCache;

    // 写缓存
    public void writeCache(List<ServiceMetaInfo> serviceCache) {
        this.serviceCache = serviceCache;
    }

    // 读缓存
    public List<ServiceMetaInfo> readCache() {
        return serviceCache;
    }

    // 清除缓存
    public void clearCache() {
        serviceCache = null;
    }
}