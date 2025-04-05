package com.lcx.rpc.cluster.fault.restrictor.provider;

import com.lcx.rpc.cluster.fault.restrictor.Restrictor;
import com.lcx.rpc.cluster.fault.restrictor.impl.TokenBucketRestrictor;

import java.util.HashMap;
import java.util.Map;

public class RestrictorProvider {
    private static final Map<String, Restrictor> restrictorMap = new HashMap<>();

    /**
     * 获取限流器
     * @param serviceName 服务名
     * @return 限流器
     */
    public static Restrictor getRateLimit(String serviceName) {
        if (!restrictorMap.containsKey(serviceName)) {
            synchronized (restrictorMap) {
                if (!restrictorMap.containsKey(serviceName)) {
                    Restrictor restrictor = new TokenBucketRestrictor(100, 10);
                    restrictorMap.put(serviceName, restrictor);
                }
            }
        }
        return restrictorMap.get(serviceName);
    }
}