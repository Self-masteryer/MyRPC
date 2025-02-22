package com.lcx.rpc.register;

import com.lcx.rpc.model.ServiceMetaInfo;

import java.util.Collection;

/**
 * 注册中心
 */
public interface Registry {

    /**
     * 注册服务
     *
     * @param serviceMetaInfo 服务节点元数据
     * @throws Exception 异常
     */
    void register(ServiceMetaInfo serviceMetaInfo) throws Exception;

    /**
     * 注销服务
     *
     * @param serviceMetaInfo 服务节点元数据
     * @throws Exception 异常
     */
    void unRegister(ServiceMetaInfo serviceMetaInfo) throws Exception;

    /**
     * 服务发现
     *
     * @param serviceKey 服务键
     * @return 服务节点元数据
     * @throws Exception 异常
     */
    Collection<ServiceMetaInfo> serviceDiscovery(String serviceKey) throws Exception;

    /**
     * 服务销毁，关闭资源
     */
    void destroy();
}
