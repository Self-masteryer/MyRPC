package com.lcx.rpc.cluster.loadbalancer.impl;

import com.lcx.rpc.cluster.loadbalancer.LoadBalancer;
import com.lcx.rpc.common.model.ServiceMetaInfo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConsistentHashLoadBalancer implements LoadBalancer {

    private static final int VIRTUAL_NODES_BASE = 160;
    private final ConcurrentHashMap<String, State> stateMap = new ConcurrentHashMap<>();

    @Override
    public ServiceMetaInfo select(Map<String, Object> params, List<ServiceMetaInfo> serviceList) {
        String serviceKey = getServiceKey(params, serviceList);
        String routingKey = (String) params.get("routingKey");
        if (routingKey == null) return null;

        return stateMap.computeIfAbsent(serviceKey, k -> new State(serviceList)).select(routingKey);
    }

    @Override
    public boolean syncState() {
        return true;
    }

    @Override
    public void refresh(String serviceKey, List<ServiceMetaInfo> serviceList) {
        State state = stateMap.get(serviceKey);
        if (state != null) {
            state.refresh(serviceList);
        } else {
            stateMap.put(serviceKey, new State(serviceList));
        }
    }

    @Override
    public void add(String serviceKey, ServiceMetaInfo serviceMetaInfo) {
        stateMap.get(serviceKey).add(serviceMetaInfo);
    }

    @Override
    public void remove(String serviceKey, ServiceMetaInfo serviceMetaInfo) {
        stateMap.get(serviceKey).remove(serviceMetaInfo);
    }

    @Override
    public void update(String serviceKey, ServiceMetaInfo oldServiceMetaInfo, ServiceMetaInfo newServiceMetaInfo) {
        stateMap.get(serviceKey).update(oldServiceMetaInfo, newServiceMetaInfo);
    }

    private static class State {
        // 哈希环
        private final TreeMap<Integer, ServiceMetaInfo> virtualNodes = new TreeMap<>();
        // 物理节点与虚拟节点映射
        private final Map<ServiceMetaInfo, Set<Integer>> nodeMapping = new HashMap<>();
        // 读写锁，保证并发安全
        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

        public State(List<ServiceMetaInfo> serviceList) {
            refresh(serviceList);
        }

        public ServiceMetaInfo select(String routingKey) {
            lock.readLock().lock();
            try {
                if (virtualNodes.isEmpty()) return null;

                int hash = getHash(routingKey);
                Map.Entry<Integer, ServiceMetaInfo> entry = virtualNodes.ceilingEntry(hash);
                if (entry == null) entry = virtualNodes.firstEntry();
                return entry.getValue();
            } finally {
                lock.readLock().unlock();
            }
        }

        public void refresh(List<ServiceMetaInfo> serviceList) {
            lock.writeLock().lock();
            try {
                virtualNodes.clear();
                nodeMapping.clear();
                serviceList.forEach(serviceMetaInfo -> add(serviceMetaInfo, false));
            } finally {
                lock.writeLock().unlock();
            }
        }

        public void add(ServiceMetaInfo serviceMetaInfo) {
            add(serviceMetaInfo, true);
        }

        public void remove(ServiceMetaInfo serviceMetaInfo) {
            lock.writeLock().lock();
            try {
                Set<Integer> hashes = nodeMapping.remove(serviceMetaInfo);
                if (hashes != null) {
                    hashes.forEach(virtualNodes::remove);
                }
            } finally {
                lock.writeLock().unlock();
            }
        }

        public void update(ServiceMetaInfo oldserviceMetaInfo, ServiceMetaInfo newserviceMetaInfo) {
            lock.writeLock().lock();
            try {
                if (!oldserviceMetaInfo.equals(newserviceMetaInfo)
                        || !Objects.equals(oldserviceMetaInfo.getWeight(), newserviceMetaInfo.getWeight())) {
                    remove(oldserviceMetaInfo);
                    add(newserviceMetaInfo, false);
                }
            } finally {
                lock.writeLock().unlock();
            }
        }

        private void add(ServiceMetaInfo serviceMetaInfo, boolean acquireLock) {
            if (acquireLock) lock.writeLock().lock();
            try {
                int virtualCount = VIRTUAL_NODES_BASE * Math.max(serviceMetaInfo.getWeight(), 0);
                Set<Integer> hashes = new HashSet<>(virtualCount);

                for (int i = 0; i < virtualCount; i++) {
                    String virtualKey = serviceMetaInfo.getServiceAddress() + "#" + i;
                    int hash = getHash(virtualKey);
                    virtualNodes.put(hash, serviceMetaInfo);
                    hashes.add(hash);
                }
                nodeMapping.put(serviceMetaInfo, hashes);
            } finally {
                if (acquireLock) lock.writeLock().unlock();
            }
        }

        private int getHash(String key) {
            final int FNV_32_PRIME = 0x811C9DC5;
            int hash = 0;

            for (int i = 0; i < key.length(); i++) {
                hash ^= key.charAt(i);
                hash *= FNV_32_PRIME;
            }
            return hash & 0x7FFFFFFF; // 保持正值
        }
    }
}