package com.lcx.rpc.register;

import cn.hutool.cron.CronUtil;
import cn.hutool.json.JSONUtil;
import com.lcx.rpc.config.RegistryConfig;
import com.lcx.rpc.config.RpcApplication;
import com.lcx.rpc.model.ServiceMetaInfo;
import io.etcd.jetcd.*;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * etcd注册中心实现类
 */
@Slf4j
public class EtcdRegistry implements Registry {

    // etcd的根节点
    private static final String ETCD_ROOT_PATH = "/rpc/";

    private Client client;
    private KV kvClient;
    private Lease leaseClient;
    private Watch watchClient;
    // 租约id: 服务节点key -> leaseId
    private final Map<String, Long> leaseIdMap = new ConcurrentHashMap<>();
    // 服务发现缓存，需监控其更新: 前缀键 -> 服务元信息列表
    private final Map<String, Map<String, ServiceMetaInfo>> serviceCache = new ConcurrentHashMap<>();

    /**
     * 连接etcd服务器，获得java客户端
     *
     * @param registryConfig 注册中心配置信息
     */
    @Override
    public void init(RegistryConfig registryConfig) {
        try {
            client = Client.builder()
                    .endpoints(registryConfig.getAddress())
                    .connectTimeout(Duration.ofMillis(registryConfig.getTimeout()))
                    .build();
            kvClient = client.getKVClient();
            leaseClient = client.getLeaseClient();
            watchClient = client.getWatchClient();
        } catch (Exception e) {
            log.error("注册中心连接失败：{}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 注册服务到ETCD
     * 通过创建一个有限期的租约来确保服务心跳机制，从而实现服务自动下线功能
     */
    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        String serviceNodeKey = serviceMetaInfo.getServiceNodeKey();
        try {
            // 创建租约
            Integer leaseTime = RpcApplication.getRpcConfig().getRegistry().getLeaseTime();
            long leaseId = leaseClient.grant(leaseTime).get().getID();
            // 设置要存储的键值对
            String key = ETCD_ROOT_PATH + serviceNodeKey;
            ByteSequence keyByteSequence = ByteSequence.from(key, StandardCharsets.UTF_8);
            ByteSequence valueByteSequence = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo), StandardCharsets.UTF_8);
            // 将键值对与租约关联
            PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
            kvClient.put(keyByteSequence, valueByteSequence, putOption).get();

            // 管理租约
            leaseIdMap.put(serviceNodeKey, leaseId);
            // 维护租约
            leaseClient.keepAlive(leaseId, new StreamObserver<>() {
                @Override
                public void onNext(LeaseKeepAliveResponse response) {
                    log.debug("租约 {} 续约成功", response.getID());
                }

                @Override
                public void onError(Throwable throwable) {
                    log.error("租约续约失败，尝试重新注册", throwable);
                    try {
                        leaseIdMap.remove(serviceNodeKey);
                        // 重新注册服务（生成新租约）
                        register(serviceMetaInfo);
                    } catch (Exception e) {
                        log.error("租约重试失败，服务可能下线", e);
                    }
                }

                @Override
                public void onCompleted() {
                    log.info("租约续约流已关闭（leaseId={}）", leaseId);
                }
            });
            log.info("服务注册成功: {}", serviceNodeKey);
        } catch (InterruptedException | ExecutionException e) {
            log.error("服务注册失败 [key={}]: {}", serviceNodeKey, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void unregister(ServiceMetaInfo serviceMetaInfo) throws Exception {
        kvClient.delete(ByteSequence.from(ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey(), StandardCharsets.UTF_8));
        Long leaseId = leaseIdMap.get(serviceMetaInfo.getServiceNodeKey());
        leaseClient.revoke(leaseId).get(); // 撤销续约
        leaseIdMap.remove(serviceMetaInfo.getServiceNodeKey());
    }

    @Override
    public Collection<ServiceMetaInfo> serviceDiscovery(String serviceKey) throws Exception {
        String preKey = ETCD_ROOT_PATH + serviceKey + "/"; // 前缀key
        // 查询缓存
        Map<String, ServiceMetaInfo> cache = serviceCache.get(preKey);
        if (cache != null) return cache.values();

        // 无缓存:双重检查锁避免线程安全问题
        synchronized (serviceCache) {
            cache = serviceCache.get(preKey);
            if (cache != null) return cache.values();

            try {
                GetOption getOption = GetOption.builder().isPrefix(true).build();
                List<KeyValue> kvs = kvClient.get(ByteSequence.from(preKey, StandardCharsets.UTF_8), getOption).get().getKvs();
                Map<String, ServiceMetaInfo> cacheMap = kvs.stream()
                        .collect(Collectors.toMap(
                                kv -> kv.getKey().toString(StandardCharsets.UTF_8),
                                kv -> JSONUtil.toBean(kv.getValue().toString(StandardCharsets.UTF_8), ServiceMetaInfo.class),
                                (oldVal, newVal) -> newVal,
                                ConcurrentHashMap::new
                        ));
                // 缓存
                serviceCache.put(preKey, cacheMap);
                watch(preKey); // 注册监听
                return cacheMap.values();
            } catch (Exception e) {
                throw new RuntimeException("获取服务列表失败", e);
            }
        }
    }

    @Override
    public void watch(String serviceKey) {
        // 已监听
        if (serviceCache.containsKey(serviceKey)) return;
        serviceCache.putIfAbsent(serviceKey, new ConcurrentHashMap<>());

        ByteSequence keyByteSequence = ByteSequence.from(serviceKey, StandardCharsets.UTF_8);
        WatchOption watchOption = WatchOption.builder().isPrefix(true).build(); // 监听前缀key
        watchClient.watch(keyByteSequence, watchOption, response -> {
            Map<String, ServiceMetaInfo> map = serviceCache.get(serviceKey);
            for (WatchEvent event : response.getEvents()) {
                String nodeKey = event.getKeyValue().getKey().toString(StandardCharsets.UTF_8);
                switch (event.getEventType()) {
                    case DELETE: // 删除
                        map.remove(nodeKey);
                        break;
                    case PUT: // 更新
                        String value = event.getKeyValue().getValue().toString(StandardCharsets.UTF_8);
                        ServiceMetaInfo serviceMetaInfo = JSONUtil.toBean(value, ServiceMetaInfo.class);
                        map.replace(nodeKey, serviceMetaInfo);
                        break;
                    default:
                }
            }
        });
    }

    @Override
    public void destroy() {
        log.info("当前节点下线");
        watchClient.close(); // 停止监听
        // 注销服务注册,并取消续约
        leaseIdMap.forEach((k, v) -> {
            ByteSequence keyByteSequence = ByteSequence.from(ETCD_ROOT_PATH + k, StandardCharsets.UTF_8);
            kvClient.delete(keyByteSequence);
            leaseClient.revoke(v);
        });
        if (client != null) client.close();
        if (kvClient != null) kvClient.close();
    }
}
