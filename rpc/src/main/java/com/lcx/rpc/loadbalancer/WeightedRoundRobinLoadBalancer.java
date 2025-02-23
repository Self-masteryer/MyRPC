package com.lcx.rpc.loadbalancer;

import com.lcx.rpc.model.ServiceMetaInfo;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 平滑加权轮询负载均衡器
 */
public class WeightedRoundRobinLoadBalancer implements LoadBalancer {

    // 服务键 -> 状态
    private final Map<String, State> currentStatusMap = new ConcurrentHashMap<>();

    @Override
    public ServiceMetaInfo select(Map<String, Object> params, List<ServiceMetaInfo> serviceMetaInfos) {
        // 获取服务键
        if (serviceMetaInfos == null || serviceMetaInfos.isEmpty()) return null;
        String serviceKey = (String) params.get("serviceKey");
        if (serviceKey == null) serviceKey = serviceMetaInfos.get(0).getServiceKey();

        State state = currentStatusMap.get(serviceKey);
        serviceMetaInfos = state.getServiceMetaInfos();
        int size = serviceMetaInfos.size();
        AtomicLong currentWeightIndex = state.getCurrentWeightIndex();

        while (true) {
            long prev = currentWeightIndex.get();
            int index = ((int) prev + 1) % size;
            int weight = (int) (prev >> 32);

            // 完成一轮遍历时，动态调整当前权重
            if (index == 0) {
                weight -= state.getGcd();
                if (weight <= 0) weight = state.getMaxWeight();
            }

            long newState = ((long) weight << 32) | (index & 0xFFFFFFFFL);
            if (currentWeightIndex.compareAndSet(prev, newState)) {
                ServiceMetaInfo serviceMetaInfo = serviceMetaInfos.get(index);
                if (serviceMetaInfo.getWeight() >= weight) {
                    return serviceMetaInfo;
                }
            }
        }

    }

    @Override
    public void refresh(String serviceKey, List<ServiceMetaInfo> serviceMetaInfoList) {
        currentStatusMap.put(serviceKey, new State(serviceMetaInfoList));
    }

    @Data
    private static class State {
        int gcd;
        int maxWeight;
        AtomicLong currentWeightIndex = new AtomicLong(0L);
        List<ServiceMetaInfo> serviceMetaInfos;

        public State(List<ServiceMetaInfo> serviceMetaInfos) {
            gcd = gcd(serviceMetaInfos);
            maxWeight = maxWeight(serviceMetaInfos);
            this.serviceMetaInfos = serviceMetaInfos;
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
