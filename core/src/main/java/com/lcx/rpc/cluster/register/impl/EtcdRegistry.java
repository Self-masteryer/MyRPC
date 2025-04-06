package com.lcx.rpc.cluster.register.impl;

import cn.hutool.json.JSONUtil;
import com.lcx.rpc.bootstrap.config.RegistryConfig;
import com.lcx.rpc.cluster.register.Registry;
import com.lcx.rpc.common.exception.RegistryException;
import com.lcx.rpc.bootstrap.config.MyRpcApplication;
import com.lcx.rpc.common.model.ServiceMetaInfo;
import com.lcx.rpc.cluster.register.cache.EtcdCacheManager;
import com.lcx.rpc.cluster.register.cache.RegistryCacheManager;
import io.etcd.jetcd.*;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.etcd.jetcd.options.PutOption;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

/**
 * etcd注册中心实现类
 */
@Slf4j
public class EtcdRegistry implements Registry {

    // 键值根路径
    public static final String ROOT_PATH = "/MyRPC/";
    // Etcd 客户端
    private final Client client;
    private final KV kvClient;
    private final Lease leaseClient;
    // 服务注册管理：节点键 -> 租约Id
    private final Map<String, Long> leaseIdMap = new ConcurrentHashMap<>();
    private final Set<ServiceMetaInfo> localServiceRegisterSet = new CopyOnWriteArraySet<>();
    // 缓存管理
    private final RegistryCacheManager cacheManager;
    // 定时全量补偿线程池
    private final ScheduledExecutorService compensationScheduler = Executors.newSingleThreadScheduledExecutor();

    {
        RegistryConfig registryConfig = MyRpcApplication.getRpcConfig().getCluster().getRegistry();
        try {
            // 1. 初始化Etcd客户端
            client = Client.builder()
                    .endpoints(registryConfig.getAddress())
                    .connectTimeout(Duration.ofMillis(registryConfig.getTimeout()))
                    .build();
            kvClient = client.getKVClient();
            leaseClient = client.getLeaseClient();

            // 2. 初始化缓存管理器
            cacheManager = new EtcdCacheManager(kvClient, client.getWatchClient());

            // 3. 启动定时全量补偿任务
            startCompensationTask(registryConfig.getCompensationInterval());
            log.info("Etcd注册中心初始化完成");
        } catch (Exception e) {
            throw new RegistryException("Etcd连接失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        String serviceNodeKey = serviceMetaInfo.getServiceNodeKey();
        String etcdServiceNodeKey = ROOT_PATH + serviceNodeKey;
        if (leaseIdMap.containsKey(etcdServiceNodeKey)) return;
        try {
            // 1. 创建租约
            long leaseId = leaseClient.grant(MyRpcApplication.getRpcConfig().getCluster().getRegistry().getLeaseTime()).get().getID();

            // 2. 注册服务节点
            ByteSequence key = ByteSequence.from(etcdServiceNodeKey, StandardCharsets.UTF_8);
            ByteSequence value = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo), StandardCharsets.UTF_8);
            PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
            kvClient.put(key, value, putOption).get();

            // 3. 维护租约
            leaseIdMap.put(etcdServiceNodeKey, leaseId);
            localServiceRegisterSet.add(serviceMetaInfo);

            // 4. 启动心跳续约
            startLeaseKeepAlive(leaseId, serviceMetaInfo);

            log.info("服务注册成功: {}", serviceNodeKey);
        } catch (Exception e) {
            log.error("服务注册失败 [{}]: {}", serviceNodeKey, e.getMessage());
            throw new RegistryException("服务注册失败", e);
        }
    }

    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        String serviceNodeKey = serviceMetaInfo.getServiceNodeKey();
        String etcdServiceNodeKey = ROOT_PATH + serviceNodeKey;
        try {
            // 1. 删除Etcd节点
            kvClient.delete(ByteSequence.from(etcdServiceNodeKey, StandardCharsets.UTF_8)).get();

            // 2. 清理本地记录
            localServiceRegisterSet.remove(serviceMetaInfo);
            Long leaseId = leaseIdMap.remove(etcdServiceNodeKey);
            if (leaseId != null) {
                leaseClient.revoke(leaseId).get();
            }

            log.info("服务注销成功: {}", serviceNodeKey);
        } catch (Exception e) {
            throw new RegistryException("服务注销失败", e);
        }
    }

    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
        return cacheManager.getServices(serviceKey);
    }

    @Override
    public void destroy() {
        log.info("Etcd注册中心关闭中...");
        // 立刻终止定期全量补偿任务
        compensationScheduler.shutdownNow();
        // 清理本地注册节点,并取消续约
        leaseIdMap.forEach((k, v) -> {
            ByteSequence keyByteSequence = ByteSequence.from(k, StandardCharsets.UTF_8);
            kvClient.delete(keyByteSequence);
            leaseClient.revoke(v);
        });

        // 关闭缓存管理器
        cacheManager.destroy();
        // 关闭Etcd客户端
        client.close();
        log.info("Etcd注册中心已关闭");
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

    //------------------------ 私有方法 ------------------------

    /**
     * 启动租约续约机制
     *
     * @param leaseId  租约Id
     * @param metaInfo 服务元信息
     */
    private void startLeaseKeepAlive(long leaseId, ServiceMetaInfo metaInfo) {
        leaseClient.keepAlive(leaseId, new StreamObserver<>() {
            @Override
            public void onNext(LeaseKeepAliveResponse response) {
                // log.debug("租约续期成功: {}", response.getID());
            }

            @Override
            public void onError(Throwable t) {
                log.error("租约续期失败,尝试重新注册服务", t);
                try {
                    leaseIdMap.remove(ROOT_PATH + metaInfo.getServiceNodeKey());
                    register(metaInfo); // 重新注册
                    log.info("服务重注册成功: {}", metaInfo.getServiceNodeKey());
                } catch (Exception e) {
                    log.error("服务重注册失败", e);
                }
            }

            @Override
            public void onCompleted() {
                log.info("租约流关闭: {}", leaseId);
            }
        });
    }

    /**
     * 启动定期补偿本地注册节点
     *
     * @param intervalMinutes 间隔时间
     */
    private void startCompensationTask(int intervalMinutes) {
        compensationScheduler.scheduleAtFixedRate(() -> {
            try {
                localServiceRegisterSet.forEach(serviceMetaInfo -> {
                    String serviceNodeKey = serviceMetaInfo.getServiceNodeKey();
                    if (!leaseIdMap.containsKey(ROOT_PATH + serviceNodeKey)) {
                        log.info("尝试补偿注册：{}", serviceNodeKey);
                        try {
                            register(serviceMetaInfo);
                        } catch (Exception e) {
                            log.error("补偿注册失败: {}", serviceNodeKey, e);
                        }
                    }
                });
            } catch (Exception e) {
                log.error("补偿任务执行异常", e);
            }
        }, intervalMinutes, intervalMinutes, TimeUnit.MINUTES);
    }
}