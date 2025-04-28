package com.lcx.rpc.bootstrap.config;

import com.lcx.rpc.cluster.fault.tolerant.TolerantStrategyKeys;
import com.lcx.rpc.cluster.loadbalancer.LoadBalancerKeys;
import lombok.Data;

/**
 * 集群配置
 */
@Data
public class ClusterConfig {

    /**
     * 注册中心
     */
    private RegistryConfig registry = new RegistryConfig();
    /**
     * 负载均衡器
     */
    private String loadBalancer = LoadBalancerKeys.ROUND_ROBIN;
    /**
     * 异常重试
     */
    private RetryConfig retry = new RetryConfig();
    /**
     * 容错策略
     */
    private String tolerantStrategy = TolerantStrategyKeys.FAIL_BACK;

}
