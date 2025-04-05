package com.lcx.rpc.cluster.register.cache;

import com.lcx.rpc.common.model.ServiceMetaInfo;

import java.util.List;

/**
 * 缓存更新监听器
 */
@FunctionalInterface
public interface CacheUpdateListener {
    void onUpdate(String serviceKey, List<ServiceMetaInfo> serviceMetaInfos);
}