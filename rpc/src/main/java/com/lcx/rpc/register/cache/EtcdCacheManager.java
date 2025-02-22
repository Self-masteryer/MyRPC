package com.lcx.rpc.register.cache;

import cn.hutool.json.JSONUtil;
import com.lcx.rpc.config.RpcApplication;
import com.lcx.rpc.model.ServiceMetaInfo;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * ETCD缓存管理实现
 */
public class EtcdCacheManager extends RegistryCacheManager {
    private final KV kvClient;
    private final Watch watchClient;

    public EtcdCacheManager(KV kvClient, Watch watchClient) {
        this.kvClient = kvClient;
        this.watchClient = watchClient;
        initScheduler(RpcApplication.getRpcConfig().getRegistry().getCompensationInterval());
    }

    @Override
    protected Map<String, ServiceMetaInfo> loadServiceData(String serviceKey) throws Exception {
        GetOption option = GetOption.builder().isPrefix(true).build();
        List<KeyValue> kvs = kvClient.get(
                ByteSequence.from(serviceKey, StandardCharsets.UTF_8),
                option
        ).get().getKvs();

        return kvs.stream().collect(Collectors.toMap(
                        kv -> kv.getKey().toString(StandardCharsets.UTF_8),
                        kv -> JSONUtil.toBean(kv.getValue().toString(StandardCharsets.UTF_8), ServiceMetaInfo.class),
                        (oldVal, newVal) -> newVal,
                        ConcurrentHashMap::new
                ));
    }

    @Override
    protected void registerWatch(String serviceKey) {
        WatchOption option = WatchOption.builder().isPrefix(true).build();
        watchClient.watch(
                ByteSequence.from(serviceKey, StandardCharsets.UTF_8),
                option,
                response -> response.getEvents().forEach(event ->
                        handleEvent(serviceKey, event)
                )
        );
    }

    @Override
    protected void handleEvent(String serviceKey, Object event) {
        WatchEvent watchEvent = (WatchEvent) event;
        String nodeKey = watchEvent.getKeyValue().getKey().toString(StandardCharsets.UTF_8);
        Map<String, ServiceMetaInfo> serviceMap = cache.get(serviceKey);

        switch (watchEvent.getEventType()) {
            case DELETE: // 删除
                serviceMap.remove(nodeKey);
                break;
            case PUT: // 更新
                ServiceMetaInfo metaInfo = JSONUtil.toBean(
                        watchEvent.getKeyValue().getValue().toString(StandardCharsets.UTF_8),
                        ServiceMetaInfo.class
                );
                serviceMap.put(nodeKey, metaInfo);
                break;
        }
        notifyListeners(serviceKey);
    }

}