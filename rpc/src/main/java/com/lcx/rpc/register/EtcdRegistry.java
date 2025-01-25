package com.lcx.rpc.register;

import cn.hutool.json.JSONUtil;
import com.lcx.rpc.config.RegisterConfig;
import com.lcx.rpc.model.ServiceMetaInfo;
import io.etcd.jetcd.*;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * etcd注册中心实现类
 */
@Slf4j
public class EtcdRegistry implements Register {

    private Client client;
    private KV kvClient;
    // etcd的根节点
    private static final String ETCD_ROOT_PATH = "/rpc/";

    /**
     * 连接etcd服务器，获得java客户端
     *
     * @param registerConfig 注册中心配置信息
     */
    @Override
    public void init(RegisterConfig registerConfig) {
        client = Client.builder()
                .endpoints(registerConfig.getAddress())
                .connectTimeout(Duration.ofMillis(registerConfig.getTimeout()))
                .build();
        kvClient = client.getKVClient();
    }

    /**
     * 注册服务到ETCD
     * 通过创建一个有限期的租约来确保服务心跳机制，从而实现服务自动下线功能
     */
    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        // 创建Lease和KV客户端
        Lease leaseClient = client.getLeaseClient();
        // 创建一个30秒租约
        long leaseId = leaseClient.grant(30).get().getID();
        // 设置要存储的键值对
        String key = ETCD_ROOT_PATH + serviceMetaInfo.getServiceKey();
        ByteSequence keyByteSequence = ByteSequence.from(key, StandardCharsets.UTF_8);
        ByteSequence valueByteSequence = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo), StandardCharsets.UTF_8);
        // 将键值对与租约关联，30秒后过期
        PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
        kvClient.put(keyByteSequence, valueByteSequence, putOption).get();
    }


    @Override
    public void unregister(ServiceMetaInfo serviceMetaInfo) throws Exception {
        kvClient.delete(ByteSequence.from(ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey(), StandardCharsets.UTF_8));
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
        if (client != null){
            client.close();
        }
        if (kvClient != null){
            kvClient.close();
        }
    }
}
