package com.lcx.rpc.config;

import com.lcx.rpc.fault.retry.RetryStrategyKeys;
import com.lcx.rpc.fault.tolerant.TolerantStrategyKeys;
import com.lcx.rpc.loadbalancer.LoadBalancerKeys;
import com.lcx.rpc.serializer.SerializerKeys;
import lombok.Data;

/**
 * 全局配置文件
 */
@Data
public class RpcConfig {

    /**
     * 环境
     */
    private String env = "dev";
    /**
     * 名称
     */
    private String name = "MyRPC";
    /**
     * 版本号
     */
    private String version = "1.0.0";
    /**
     * 服务器主机名
     */
    private String host = "localhost";
    /**
     * 服务器端口
     */
    private Integer port = 8080;
    /**
     * 权重
     */
    private Integer weight = 1;
    /**
     * mock
     */
    private boolean mock = false;
    /**
     * 序列化器
     */
    private String serializer = SerializerKeys.KRYO;
    /**
     * 负载均衡器
     */
    private String loadBalancer = LoadBalancerKeys.ROUND_ROBIN;
    /**
     * 重试策略
     */
    private String retryStrategy = RetryStrategyKeys.NO;
    /**
     * 容错策略
     */
    private String tolerantStrategy = TolerantStrategyKeys.FAIL_BACK;
    /**
     * 注册中心
     */
    private RegistryConfig registry = new RegistryConfig();
}
