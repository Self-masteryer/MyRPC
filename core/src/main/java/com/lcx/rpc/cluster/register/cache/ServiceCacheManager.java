package com.lcx.rpc.cluster.register.cache;

import com.lcx.rpc.cluster.loadbalancer.LoadBalancer;
import com.lcx.rpc.cluster.loadbalancer.LoadBalancerFactory;
import com.lcx.rpc.common.exception.RegistryException;
import com.lcx.rpc.common.model.ServiceMetaInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;

/**
 * 服务缓存管理器
 */
@Slf4j
public abstract class ServiceCacheManager {

    // 终止标志
    protected volatile boolean isDestroyed = false;
    // 服务发现缓存：服务键 -> 服务
    protected final Map<String, List<ServiceMetaInfo>> cache = new ConcurrentHashMap<>();
    // 缓存更新监听器
    private final List<CacheUpdateListener> listeners = new CopyOnWriteArrayList<>();
    // 定期全量补偿线程池
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     * 初始化
     *
     * @param intervalMinutes 间隔时间
     */
    protected void init(int intervalMinutes) {
        // 启动定期全量补偿任务
        scheduler.scheduleAtFixedRate(
                this::compensationAllCache,
                intervalMinutes,
                intervalMinutes,
                TimeUnit.MINUTES
        );
        // 添加负载均衡监听器：更新状态
        if (LoadBalancerFactory.loadBalancer.syncState()) {
            addListener(new CacheUpdateListener() {

                final LoadBalancer loadBalancer = LoadBalancerFactory.loadBalancer;

                @Override
                public void refresh(String serviceKey, List<ServiceMetaInfo> serviceMetaInfos) {
                    loadBalancer.refresh(serviceKey, serviceMetaInfos);
                }

                @Override
                public void add(String serviceKey, ServiceMetaInfo serviceMetaInfo) {
                    loadBalancer.add(serviceKey, serviceMetaInfo);
                }

                @Override
                public void remove(String serviceKey, ServiceMetaInfo serviceMetaInfo) {
                    loadBalancer.remove(serviceKey, serviceMetaInfo);
                }

                @Override
                public void update(String serviceKey, ServiceMetaInfo oldServiceMetaInfo, ServiceMetaInfo newServiceMetaInfo) {
                    loadBalancer.update(serviceKey, oldServiceMetaInfo, newServiceMetaInfo);
                }
            });
        }
    }

    /**
     * 获取服务列表：懒加载
     */
    public List<ServiceMetaInfo> getServiceList(String serviceKey) {
        if (!cache.containsKey(serviceKey)) {
            // 双重检测锁：只允许一个线程获取服务列表
            synchronized (serviceKey.intern()) {
                if (!cache.containsKey(serviceKey)) {
                    try {
                        // 首次从注册中心获取服务列表并缓存
                        List<ServiceMetaInfo> serviceList = loadServices(serviceKey);
                        // 初始化负载均衡算法状态
                        refreshNotify(serviceKey, serviceList);
                        // 注册监听器，追踪后续事件，维护缓存和算法状态
                        registerWatcher(serviceKey);
                    } catch (Exception e) {
                        throw new RegistryException("服务加载失败: " + serviceKey, e);
                    }
                }
            }
        }
        return cache.get(serviceKey);
    }

    /**
     * 全量补偿任务
     */
    private void compensationAllCache() {
        if (isDestroyed) return;
        cache.keySet().forEach(serviceKey -> {
            try {
                synchronized (serviceKey.intern()) {
                    // 注销监听器，防止事件处理与全量补偿双写并发异常
                    unRegisterWatcher(serviceKey);
                    // 重新获取服务列表
                    List<ServiceMetaInfo> serviceList = loadServices(serviceKey);
                    // 刷新算法状态
                    refreshNotify(serviceKey, serviceList);
                    // 注册监听器，追踪后续事件，维护缓存和算法状态
                    registerWatcher(serviceKey);
                    log.info("缓存补偿完成: {} nodes={}", serviceKey, serviceList.size());
                }
            } catch (Exception e) {
                log.error("补偿失败: {}", serviceKey, e);
            }
        });
    }

    /**
     * 加载服务（抽象方法）
     *
     * @param serviceKey 服务键
     * @return 服务元信息列表
     * @throws Exception 异常
     */
    protected abstract List<ServiceMetaInfo> loadServices(String serviceKey) throws Exception;

    /**
     * 注册监听（抽象方法）：根据具体注册中心实现处理监听注册
     * 需要保证幂等性
     *
     * @param serviceKey 服务键
     */
    protected abstract void registerWatcher(String serviceKey);

    /**
     * 注销监听（抽象方法）：拉取新的服务列表，需要关闭之前监听器，避免写写并发异常
     *
     * @param serviceKey 服务键
     */
    protected abstract void unRegisterWatcher(String serviceKey);

    public void addListener(CacheUpdateListener listener) {
        listeners.add(listener);
    }

    protected void refreshNotify(String serviceKey, List<ServiceMetaInfo> serviceList) {
        listeners.forEach(listener -> listener.refresh(serviceKey, serviceList));
    }

    protected void addNotify(String serviceKey, ServiceMetaInfo serviceMetaInfo) {
        listeners.forEach(listener -> listener.add(serviceKey, serviceMetaInfo));
    }

    protected void removeNotify(String serviceKey, ServiceMetaInfo serviceMetaInfo) {
        listeners.forEach(listener -> listener.remove(serviceKey, serviceMetaInfo));
    }

    protected void updateNotify(String serviceKey, ServiceMetaInfo oldServiceMetaInfo, ServiceMetaInfo newServiceMetaInfo) {
        listeners.forEach(listener -> listener.update(serviceKey, oldServiceMetaInfo, newServiceMetaInfo));
    }

    /**
     * 销毁
     */
    public void destroy() {
        isDestroyed = true;
        scheduler.shutdownNow();
        listeners.clear();
        cache.clear();
    }
}