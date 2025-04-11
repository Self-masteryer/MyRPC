package com.lcx.rpc.cluster.register.cache;

import com.lcx.rpc.common.model.ServiceMetaInfo;

import java.util.List;

/**
 * 缓存更新监听器
 */
public interface CacheUpdateListener {

    void refresh(String serviceKey, List<ServiceMetaInfo> serviceMetaInfos);

    void add(String serviceKey, ServiceMetaInfo serviceMetaInfo);

    void remove(String serviceKey, ServiceMetaInfo serviceMetaInfo);

    void update(String serviceKey, ServiceMetaInfo oldServiceMetaInfo, ServiceMetaInfo newServiceMetaInfo);
}