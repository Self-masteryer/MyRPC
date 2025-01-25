package com.lcx.rpc.register;

import com.lcx.rpc.config.RegistryConfig;
import com.lcx.rpc.model.ServiceMetaInfo;

import java.util.List;

/**
 * 注册中心
 */
public interface Registry {

    /**
     * 初始化
     */
    void init(RegistryConfig registryConfig);

    /**
     * 注册服务(服务端)
     *
     * @param serviceMetaInfo 服务元数据
     * @throws Exception 异常
     */
    void register(ServiceMetaInfo serviceMetaInfo) throws Exception;

    /**
     * 注销服务(服务端)
     *
     * @param serviceMetaInfo 服务元数据
     * @throws Exception 异常
     */
    void unregister(ServiceMetaInfo serviceMetaInfo) throws Exception;

    /**
     * 服务发现(服务端)
     *
     * @param serviceKey 服务键
     * @return 服务元数据
     * @throws Exception 异常
     */
    List<ServiceMetaInfo> serviceDiscovery(String serviceKey) throws Exception;

    /**
     * 服务销毁
     */
    void destroy();
}
