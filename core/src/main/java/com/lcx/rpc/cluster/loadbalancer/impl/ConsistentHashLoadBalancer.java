package com.lcx.rpc.cluster.loadbalancer.impl;

import com.lcx.rpc.cluster.loadbalancer.LoadBalancer;
import com.lcx.rpc.common.model.ServiceMetaInfo;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 一致性hash负载均衡器
 */
public class ConsistentHashLoadBalancer implements LoadBalancer {

    // 一致性哈希环，存放虚拟节点
    private final TreeMap<Long, ServiceMetaInfo> virtualNodes = new TreeMap<>();
    // hash算法
    private final MessageDigest md = MessageDigest.getInstance("MD5");
    // 虚拟节点个数
    private static final int VIRTUAL_NODE_SIZE = 100;

    public ConsistentHashLoadBalancer() throws NoSuchAlgorithmException {
    }

    @Override
    public ServiceMetaInfo select(Map<String, Object> params, List<ServiceMetaInfo> serviceList) {
        if (serviceList == null || serviceList.isEmpty()) return null;

        // 添加虚拟节点，构造一致性hash环
        for (ServiceMetaInfo serviceMetaInfo : serviceList) {
            for (int i = 0; i < VIRTUAL_NODE_SIZE; i++) {
                String key = serviceMetaInfo.getServiceNodeKey() + "#VN" + i;
                virtualNodes.put(getHash(key), serviceMetaInfo);
            }
        }

        long hash = getHash(params.toString());
        Map.Entry<Long, ServiceMetaInfo> entry = virtualNodes.ceilingEntry(hash);
        if (entry == null) return virtualNodes.firstEntry().getValue();
        return entry.getValue();
    }

    @Override
    public void refresh(String serviceKey, List<ServiceMetaInfo> serviceList) {

    }

    @Override
    public void add(String serviceKey, ServiceMetaInfo serviceMetaInfo) {

    }

    @Override
    public void remove(String serviceKey, ServiceMetaInfo serviceMetaInfo) {

    }

    @Override
    public void update(String serviceKey, ServiceMetaInfo oldServiceMetaInfo, ServiceMetaInfo newServiceMetaInfo) {

    }

    /**
     * 计算字符串的哈希值（0 ~ 2^32-1）
     */
    private long getHash(String key) {
        md.reset();
        md.update(key.getBytes());
        byte[] digest = md.digest();
        // 将哈希值转换为 32 位整数（取前 4 字节）
        long hash = ((long) (digest[3] & 0xFF) << 24) |
                ((long) (digest[2] & 0xFF) << 16) |
                ((long) (digest[1] & 0xFF) << 8) |
                ((long) (digest[0] & 0xFF));
        return hash & 0xFFFFFFFFL; // 确保结果为无符号 32 位
    }
}
