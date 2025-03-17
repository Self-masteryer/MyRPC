package com.lcx.rpc.register;

import cn.hutool.core.collection.ConcurrentHashSet;

import com.lcx.rpc.config.RegistryConfig;
import com.lcx.rpc.config.RpcApplication;
import com.lcx.rpc.model.ServiceMetaInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.vertx.core.http.impl.HttpClientConnection.log;

/**
 * zookeeper注册中心实现
 */
public class ZooKeeperRegistry implements Registry {

    public static final String ROOT_PATH = "/MyRPC";

    private CuratorFramework client;
    private ServiceDiscovery<ServiceMetaInfo> serviceDiscovery;
    // 本机注册的节点 key 集合（用于维护续期）
    private final Set<String> localRegisterNodeKeySet = new HashSet<>();
    // 注册中心服务缓存
    private final RegistryServiceCache registryServiceCache = new RegistryServiceCache();
    // 正在监听的 key 集合
    private final Set<String> watchingKeySet = new ConcurrentHashSet<>();

    {
        RegistryConfig registryConfig = RpcApplication.getRpcConfig().getRegistry();
        // 构建 client 实例
        client = CuratorFrameworkFactory
                .builder()
                .connectString(registryConfig.getAddress())
                .sessionTimeoutMs(40000) // 设置会话超时时间
                // zk还会根据minSessionTimeout与maxSessionTimeout两个参数重新调整最后的超时值。默认分别为tickTime 的2倍和20倍
                .retryPolicy(new ExponentialBackoffRetry(1000, 3)) // 指数时间重试
                .build();
        // 构建 serviceDiscovery 实例
        serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceMetaInfo.class)
                .client(client)
                .basePath(ROOT_PATH)
                .serializer(new JsonInstanceSerializer<>(ServiceMetaInfo.class))
                .build();
        try {
            // 启动 client 和 serviceDiscovery
            client.start();
            serviceDiscovery.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        // 注册到 zk 里
        serviceDiscovery.registerService(buildServiceInstance(serviceMetaInfo));
        // 添加节点信息到本地缓存
        String registerKey = ROOT_PATH + "/" + serviceMetaInfo.getServiceNodeKey();
        localRegisterNodeKeySet.add(registerKey);
    }

    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        try {
            serviceDiscovery.unregisterService(buildServiceInstance(serviceMetaInfo));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // 从本地缓存移除
        String registerKey = ROOT_PATH + "/" + serviceMetaInfo.getServiceNodeKey();
        localRegisterNodeKeySet.remove(registerKey);
    }

    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
        // 优先从缓存获取服务
        List<ServiceMetaInfo> cachedServiceMetaInfoList = registryServiceCache.readCache();
        if (cachedServiceMetaInfoList != null) return cachedServiceMetaInfoList;

        synchronized (registryServiceCache) {
            cachedServiceMetaInfoList = registryServiceCache.readCache();
            if (cachedServiceMetaInfoList != null) return cachedServiceMetaInfoList;

            try {
                // List<String> strings = client.getChildren().forPath("/" + serviceKey);
                // 查询服务信息
                Collection<ServiceInstance<ServiceMetaInfo>> serviceInstanceList = serviceDiscovery.queryForInstances(serviceKey);
                // 解析服务信息
                List<ServiceMetaInfo> serviceMetaInfoList = serviceInstanceList.stream()
                        .map(ServiceInstance::getPayload)
                        .collect(Collectors.toList());
                // 写入服务缓存
                registryServiceCache.writeCache(serviceMetaInfoList);
                return serviceMetaInfoList;
            } catch (Exception e) {
                throw new RuntimeException("获取服务列表失败", e);
            }
        }
    }

    private void watch(String serviceNodeKey) {
        String watchKey = ROOT_PATH + "/" + serviceNodeKey;
        if (!watchingKeySet.contains(watchKey)) {
            CuratorCache curatorCache = CuratorCache.build(client, watchKey);
            curatorCache.listenable().addListener(
                    CuratorCacheListener
                            .builder()
                            .forDeletes(childData -> registryServiceCache.clearCache())
                            .forChanges(((oldNode, node) -> registryServiceCache.clearCache()))
                            .build()
            );
            curatorCache.start();
        }
    }

    @Override
    public void destroy() {
        log.info("当前节点下线");
        // 下线节点（这一步可以不做，因为都是临时节点，服务下线，自然就被删掉了）
        for (String key : localRegisterNodeKeySet) {
            try {
                client.delete().guaranteed().forPath(key);
            } catch (Exception e) {
                throw new RuntimeException(key + "节点下线失败");
            }
        }
        // 释放资源
        if (client != null) client.close();
    }

    /**
     * 返回与注册中心实现适配的键
     *
     * @param key 键名
     * @return 适配键
     */
    public static String adaptKey(String key) {
        return ROOT_PATH + key;
    }

    private ServiceInstance<ServiceMetaInfo> buildServiceInstance(ServiceMetaInfo serviceMetaInfo) {
        String serviceAddress = String.format("%s:%s", serviceMetaInfo.getHost(), serviceMetaInfo.getPort());
        try {
            return ServiceInstance.<ServiceMetaInfo>builder()
                    .id(serviceAddress)
                    .name(serviceMetaInfo.getServiceKey())
                    .address(serviceAddress)
                    .payload(serviceMetaInfo)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}