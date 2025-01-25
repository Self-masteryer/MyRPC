package com.lcx.rpc.register;

import cn.hutool.json.JSONUtil;
import com.lcx.rpc.config.RegistryConfig;
import com.lcx.rpc.config.RpcApplication;
import com.lcx.rpc.model.ServiceMetaInfo;
import io.etcd.jetcd.*;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * etcd注册中心实现类
 */
@Slf4j
public class EtcdRegistry implements Registry {

    private Client client;
    private KV kvClient;
    // etcd的根节点
    private static final String ETCD_ROOT_PATH = "/rpc/";
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     * 连接etcd服务器，获得java客户端
     *
     * @param registryConfig 注册中心配置信息
     */
    @Override
    public void init(RegistryConfig registryConfig) {
        client = Client.builder()
                .endpoints(registryConfig.getAddress())
                .connectTimeout(Duration.ofMillis(registryConfig.getTimeout()))
                .build();
        kvClient = client.getKVClient();
    }

    /**
     * 注册服务到ETCD
     * 通过创建一个有限期的租约来确保服务心跳机制，从而实现服务自动下线功能
     */
    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        // 创建租约
        Lease leaseClient = client.getLeaseClient();
        Integer leaseTime = RpcApplication.getRpcConfig().getRegistry().getLeaseTime();
        long leaseId = leaseClient.grant(leaseTime).get().getID();

        // 启动租约续期定时任务
        scheduler.scheduleAtFixedRate(() -> {
            try {
                leaseClient.keepAliveOnce(leaseId);
            } catch (Exception e) {
                log.error("Failed to renew lease for service: {}", serviceMetaInfo.getServiceKey(), e);
            }
        }, leaseTime - 5, leaseTime - 5, TimeUnit.SECONDS);  // 提前5秒续期

        // 设置要存储的键值对
        String key = ETCD_ROOT_PATH + serviceMetaInfo.getServiceKey();
        ByteSequence keyByteSequence = ByteSequence.from(key, StandardCharsets.UTF_8);
        ByteSequence valueByteSequence = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo), StandardCharsets.UTF_8);
        // 将键值对与租约关联
        PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
        kvClient.put(keyByteSequence, valueByteSequence, putOption).get();
    }

    @Override
    public void unregister(ServiceMetaInfo serviceMetaInfo) throws Exception {
        kvClient.delete(ByteSequence.from(ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey(), StandardCharsets.UTF_8));
        scheduler.shutdown();
    }

    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) throws Exception {
        String key = ETCD_ROOT_PATH + serviceKey + "/";
        GetOption getOption = GetOption.builder().isPrefix(true).build();
        try {
            List<KeyValue> kvs = kvClient.get(ByteSequence.from(key, StandardCharsets.UTF_8), getOption).get().getKvs();
            return kvs.stream()
                    .map(kv -> JSONUtil.toBean(kv.getValue().toString(), ServiceMetaInfo.class))
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("获取服务列表失败", e);
        }
    }

    @Override
    public void destroy() {
        log.info("当前节点下线");
        if (client != null) {
            client.close();
        }
        if (kvClient != null) {
            kvClient.close();
        }
    }
}
