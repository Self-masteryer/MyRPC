package com.lcx.rpc.register.cache;

import cn.hutool.json.JSONUtil;
import com.lcx.rpc.config.RpcApplication;
import com.lcx.rpc.model.ServiceMetaInfo;
import com.lcx.rpc.register.EtcdRegistry;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * ETCD缓存管理实现
 */
public class EtcdCacheManager extends RegistryCacheManager {
    private static final Logger log = LoggerFactory.getLogger(EtcdCacheManager.class);
    private final KV kvClient;
    private final Watch watchClient;
    // 服务键 -> Watcher
    private final Map<String, Watch.Watcher> activeWatchers = new ConcurrentHashMap<>();

    public EtcdCacheManager(KV kvClient, Watch watchClient) {
        this.kvClient = kvClient;
        this.watchClient = watchClient;
        init(RpcApplication.getRpcConfig().getRegistry().getCompensationInterval());
    }

    @Override
    protected List<ServiceMetaInfo> loadServiceData(String serviceKey) throws Exception {
        String etcdServiceKey = EtcdRegistry.ROOT_PATH + serviceKey;
        GetOption option = GetOption.builder().isPrefix(true).build();
        List<KeyValue> kvs = kvClient.get(
                ByteSequence.from(etcdServiceKey, StandardCharsets.UTF_8),
                option
        ).get().getKvs();

        return kvs.stream()
                .map(kv -> JSONUtil.toBean(kv.getValue().toString(StandardCharsets.UTF_8), ServiceMetaInfo.class))
                .collect(Collectors.toList());
    }

    @Override
    protected void registerWatch(String serviceKey) {
        String etcdServiceKey = EtcdRegistry.ROOT_PATH + serviceKey;
        if (activeWatchers.containsKey(etcdServiceKey)) return;
        lock.lock();
        try {
            if (activeWatchers.containsKey(etcdServiceKey)) return;
            WatchOption option = WatchOption.builder().isPrefix(true).build();
            Watch.Watcher watcher = watchClient.watch(
                    ByteSequence.from(etcdServiceKey, StandardCharsets.UTF_8),
                    option,
                    response -> response.getEvents().forEach(event ->
                            handleEvent(serviceKey, event)
                    )
            );
            activeWatchers.put(etcdServiceKey, watcher);
        } finally {
            lock.unlock();
        }
    }

    @Override
    protected void handleEvent(String serviceKey, Object event) {
        WatchEvent watchEvent = (WatchEvent) event;
        ServiceMetaInfo preMetaInfo = JSONUtil.toBean(
                watchEvent.getPrevKV().getValue().toString(StandardCharsets.UTF_8),
                ServiceMetaInfo.class);
        List<ServiceMetaInfo> ServiceMetaInfos = cache.get(serviceKey);

        switch (watchEvent.getEventType()) {
            case DELETE: // 删除
                ServiceMetaInfos.remove(preMetaInfo);
            case PUT: // 更新
                ServiceMetaInfo metaInfo = JSONUtil.toBean(
                        watchEvent.getKeyValue().getValue().toString(StandardCharsets.UTF_8),
                        ServiceMetaInfo.class);
                ServiceMetaInfos.add(metaInfo);
                break;
            case UNRECOGNIZED: // 未知
            default:
                break;
        }
        notifyListeners(serviceKey);
    }

    @Override
    public void destroy() {
        // 关闭监听
        activeWatchers.forEach((k, v) -> v.close());
        super.destroy();
    }
}