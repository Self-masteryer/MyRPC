package com.lcx.rpc.cluster.register.cache;

import com.lcx.rpc.cluster.loadbalancer.LoadBalancerFactory;
import com.lcx.rpc.common.exception.RegistryException;
import com.lcx.rpc.common.model.ServiceMetaInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 注册中心缓存管理器
 */
@Slf4j
public abstract class RegistryCacheManager {

    protected volatile boolean isDestroyed = false;
    // 服务发现缓存：服务键 -> 服务
    protected final Map<String, List<ServiceMetaInfo>> cache = new ConcurrentHashMap<>();
    // 监听器：更新负载均衡器状态
    private final List<CacheUpdateListener> listeners = new CopyOnWriteArrayList<>();
    // 定期全量补偿线程池
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    // 可重入锁
    protected final ReentrantLock lock = new ReentrantLock();

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
        addListener(LoadBalancerFactory.loadBalancer::refresh);
    }

    /**
     * 加载服务数据（抽象方法）
     *
     * @param serviceKey 服务键
     * @return 服务元信息列表
     * @throws Exception 异常
     */
    protected abstract List<ServiceMetaInfo> loadServiceData(String serviceKey) throws Exception;

    /**
     * 注册监听器（抽象方法）:一个服务键只需注册一次
     *
     * @param serviceKey 服务键
     */
    protected abstract void registerWatch(String serviceKey);

    /**
     * 处理事件（抽象方法）
     *
     * @param serviceKey 服务键
     * @param event      事件
     */
    protected abstract void handleEvent(String serviceKey, Object event);

    /**
     * 获取服务列表
     */
    public List<ServiceMetaInfo> getServices(String serviceKey) {
        if (!cache.containsKey(serviceKey)) {
            loadAndWatch(serviceKey);
        }
        return cache.get(serviceKey);
    }

    /**
     * 加载并监听服务
     */
    public void loadAndWatch(String serviceKey) {
        // 双重检测锁:只允许一个线程更新缓存
        lock.lock();
        if (cache.containsKey(serviceKey)) return;

        try {
            cache.put(serviceKey, loadServiceData(serviceKey));
            registerWatch(serviceKey);
            notifyListeners(serviceKey);
        } catch (Exception e) {
            throw new RegistryException("服务加载失败: " + serviceKey, e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 全量补偿任务
     */
    private void compensationAllCache() {
        if (isDestroyed) return;
        cache.keySet().forEach(serviceKey -> {
            try {
                List<ServiceMetaInfo> serviceMetaInfos = loadServiceData(serviceKey);
                cache.put(serviceKey, serviceMetaInfos);
                log.info("缓存补偿完成: {} nodes={}", serviceKey, serviceMetaInfos.size());
            } catch (Exception e) {
                log.error("补偿失败: {}", serviceKey, e);
            }
        });
    }

    /**
     * 通知监听器
     */
    protected void notifyListeners(String serviceKey) {
        listeners.forEach(listener ->
                listener.onUpdate(serviceKey, cache.get(serviceKey))
        );
    }

    /**
     * 添加监听器
     */
    public void addListener(CacheUpdateListener listener) {
        listeners.add(listener);
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