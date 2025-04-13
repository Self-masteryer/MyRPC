package com.lcx.rpc.cluster.loadbalancer.impl;

import com.lcx.rpc.cluster.loadbalancer.LoadBalancer;
import com.lcx.rpc.common.model.ServiceMetaInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 平滑加权轮询负载均衡器
 */
@Slf4j
public class WeightedRoundRobinLoadBalancer implements LoadBalancer {

    private final Map<String, State> stateMap = new ConcurrentHashMap<>();

    @Override
    public ServiceMetaInfo select(Map<String, Object> params, List<ServiceMetaInfo> serviceList) {
        if (serviceList == null || serviceList.isEmpty()) return null;
        String serviceKey = getServiceKey(params, serviceList);

        return stateMap.computeIfAbsent(serviceKey, k -> new State(serviceList)).select();
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
        private int gcd;  // 最大公约数
        private int maxWeight; // 最大权重
        private final List<ServiceMetaInfo> serviceList; // 服务列表缓存的引用
        private AtomicLong currentWeightIndex = new AtomicLong(0L); // 当前下标和权重
        private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);

        public State(List<ServiceMetaInfo> serviceMetaInfos) {
            this.serviceList = serviceMetaInfos;
            gcd = gcd(serviceList);
            maxWeight = maxWeight(serviceList);
        }

        public ServiceMetaInfo select() {
            readWriteLock.readLock().lock();
            try {
                int size = serviceList.size();

                while (true) {
                    long prev = currentWeightIndex.get();
                    int index = ((int) prev + 1) % size; // 当前下标
                    int weight = (int) (prev >> 32);     // 当前权重

                    // 完成一轮遍历时，动态调整当前权重
                    if (index == 0) {
                        weight -= gcd;
                        if (weight <= 0) weight = maxWeight;
                    }

                    long newState = ((long) weight << 32) | (index & 0xFFFFFFFFL);
                    if (currentWeightIndex.compareAndSet(prev, newState)) {
                        ServiceMetaInfo serviceMetaInfo = serviceList.get(index);
                        if (serviceMetaInfo.getWeight() >= weight) {
                            return serviceMetaInfo;
                        }
                    }
                }
            } finally {
                readWriteLock.readLock().unlock();
            }
        }

        public void refresh(List<ServiceMetaInfo> serviceList) {
            readWriteLock.writeLock().lock();
            try {
                gcd = gcd(serviceList);
                maxWeight = maxWeight(serviceList);
                currentWeightIndex = new AtomicLong(0L);
            } finally {
                readWriteLock.writeLock().unlock();
            }

        }

        public void add(ServiceMetaInfo serviceMetaInfo) {
            readWriteLock.writeLock().lock();
            try {
                gcd = gcd(serviceList);
                if (serviceMetaInfo.getWeight() > maxWeight) {
                    maxWeight = serviceMetaInfo.getWeight();
                }
            } finally {
                readWriteLock.writeLock().unlock();
            }
        }

        public void remove(ServiceMetaInfo serviceMetaInfo) {
            readWriteLock.writeLock().lock();
            try {
                gcd = gcd(serviceList); // 刷新最大公约数
                if (serviceMetaInfo.getWeight() == maxWeight) {
                    maxWeight = maxWeight(serviceList);
                }
            } finally {
                readWriteLock.writeLock().unlock();
            }
        }

        public void update(ServiceMetaInfo oldServiceMetaInfo, ServiceMetaInfo newServiceMetaInfo) {
            readWriteLock.writeLock().lock();
            try {
                if (!Objects.equals(oldServiceMetaInfo.getWeight(), newServiceMetaInfo.getWeight())) {
                    gcd = gcd(serviceList); // 不相等，刷新最大公约数
                }
                if (newServiceMetaInfo.getWeight() >= maxWeight) {
                    maxWeight = newServiceMetaInfo.getWeight();
                } else if (oldServiceMetaInfo.getWeight() == maxWeight) {
                    maxWeight = maxWeight(serviceList);
                }
            } finally {
                readWriteLock.writeLock().unlock();
            }
        }

        /**
         * 最大公约数
         */
        private static int gcd(List<ServiceMetaInfo> serviceMetaInfoList) {
            return serviceMetaInfoList.stream()
                    .mapToInt(ServiceMetaInfo::getWeight)
                    .reduce(0, (a, b) -> {
                        while (b != 0) {
                            int temp = b;
                            b = a % b;
                            a = temp;
                        }
                        return a;
                    });
        }

        /**
         * 最大权重
         */
        private static int maxWeight(List<ServiceMetaInfo> serviceMetaInfos) {
            int maxWeight = 0;
            for (ServiceMetaInfo serviceMetaInfo : serviceMetaInfos) {
                if (serviceMetaInfo.getWeight() > maxWeight) {
                    maxWeight = serviceMetaInfo.getWeight();
                }
            }
            return maxWeight;
        }
    }
}
