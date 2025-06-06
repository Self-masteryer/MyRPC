package com.lcx.rpc.cluster.loadbalancer;

import com.lcx.rpc.bootstrap.config.MyRpcApplication;
import com.lcx.rpc.common.spi.SpiLoader;

/**
 * 负载均衡器工厂
 */
public class LoadBalancerFactory {

    static {
        SpiLoader.load(LoadBalancer.class);
    }

    /**
     * 配置的负载均衡器
     */
    public static final LoadBalancer loadBalancer = getLoadBalancer(MyRpcApplication.getRpcConfig().getCluster().getLoadBalancer());

    /**
     * 获取负载均衡器实例
     * @param key 负载均衡器key值
     * @return 负载均衡器
     */
    public static LoadBalancer getLoadBalancer(String key) {
        return SpiLoader.getInstance(LoadBalancer.class, key);
    }
}
