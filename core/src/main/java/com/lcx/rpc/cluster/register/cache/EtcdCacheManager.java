package com.lcx.rpc.cluster.register.cache;

import cn.hutool.json.JSONUtil;
import com.lcx.rpc.bootstrap.config.MyRpcApplication;
import com.lcx.rpc.cluster.register.impl.EtcdRegistry;
import com.lcx.rpc.common.model.ServiceMetaInfo;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Etcd缓存管理实现
 */
@Slf4j
public class EtcdCacheManager extends ServiceCacheManager {
    private final KV kvClient;
    private final Watch watchClient;
    // 服务键 -> Watcher
    private final Map<String, Watch.Watcher> activeWatchers = new ConcurrentHashMap<>();
    private ExecutorService eventHandler;

    public EtcdCacheManager(KV kvClient, Watch watchClient) {
        this.kvClient = kvClient;
        this.watchClient = watchClient;
        init(MyRpcApplication.getRpcConfig().getCluster().getRegistry().getCompensationInterval());
    }

    @Override
    protected List<ServiceMetaInfo> loadServices(String serviceKey) throws Exception {
        String etcdServiceKey = EtcdRegistry.ROOT_PATH + serviceKey;
        GetOption option = GetOption.builder().isPrefix(true).build();
        List<ServiceMetaInfo> serviceList = cache.computeIfAbsent(serviceKey, k -> new CopyOnWriteArrayList<>());
        // 从注册中心获取服务列表
        List<KeyValue> kvs = kvClient.get(
                ByteSequence.from(etcdServiceKey, StandardCharsets.UTF_8),
                option
        ).get().getKvs();

        // 反序列化
        List<ServiceMetaInfo> temp = new ArrayList<>();
        for (KeyValue kv : kvs) {
            temp.add(JSONUtil.toBean(kv.getValue().toString(StandardCharsets.UTF_8), ServiceMetaInfo.class));
        }

        serviceList.clear();
        serviceList.addAll(temp);
        return serviceList;

    }

    @Override
    protected void registerWatcher(String serviceKey) {
        String etcdServiceKey = EtcdRegistry.ROOT_PATH + serviceKey;
        if (activeWatchers.containsKey(etcdServiceKey)) return;
        WatchOption option = WatchOption.builder().isPrefix(true).build();
        // 基于 HTTP/2 的 gRPC 流式推送机制，每个 Watcher 对应一个独立的流（Stream），同一流内的事件按服务端生成的顺序传输，避免乱序
        Watch.Watcher watcher = watchClient.watch(
                ByteSequence.from(etcdServiceKey, StandardCharsets.UTF_8),
                option, // 注意这里对于事件的处理为多线程
                response -> response.getEvents().forEach(event ->
                        // 单线程化处理，保证按发送顺序处理
                        eventHandler.execute(() -> handleEvent(serviceKey, event))
                )
        );
        activeWatchers.put(etcdServiceKey, watcher);
        eventHandler = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1024));
    }

    @Override
    protected void unRegisterWatcher(String serviceKey) {
        Watch.Watcher watcher = activeWatchers.get(serviceKey);
        if (watcher != null) watcher.close();
        eventHandler.shutdownNow();
    }

    protected void handleEvent(String serviceKey, Object event) {
        List<ServiceMetaInfo> serviceList = cache.get(serviceKey);
        WatchEvent watchEvent = (WatchEvent) event;
        ServiceMetaInfo preMetaInfo = JSONUtil.toBean(
                watchEvent.getPrevKV().getValue().toString(StandardCharsets.UTF_8),
                ServiceMetaInfo.class);

        // 注意处理事件为原子操作
        switch (watchEvent.getEventType()) {
            case DELETE: // 删除
                serviceList.remove(preMetaInfo);
                removeNotify(serviceKey, preMetaInfo);
                break;
            case PUT: // 新增或更新
                ServiceMetaInfo curMetaInfo = JSONUtil.toBean(
                        watchEvent.getKeyValue().getValue().toString(StandardCharsets.UTF_8),
                        ServiceMetaInfo.class);
                if (preMetaInfo != null) { // 更新
                    serviceList.remove(preMetaInfo);
                    serviceList.add(curMetaInfo);
                    updateNotify(serviceKey, preMetaInfo, curMetaInfo);
                } else { // 新增
                    serviceList.add(curMetaInfo);
                    addNotify(serviceKey, curMetaInfo);
                }
                break;
            case UNRECOGNIZED: // 未知
                log.warn("Unrecognized event type: {}", watchEvent.getEventType());
            default:
                break;

        }
    }

    @Override
    public void destroy() {
        // 关闭监听
        activeWatchers.forEach((k, v) -> v.close());
        super.destroy();
    }
}