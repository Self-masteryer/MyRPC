package com.lcx.rpc.bootstrap.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import cn.hutool.core.collection.CollUtil;
import com.lcx.rpc.bootstrap.config.MyRpcApplication;
import com.lcx.rpc.bootstrap.config.MyRpcConfig;
import com.lcx.rpc.cluster.fault.circuitBreaker.CircuitBreaker;
import com.lcx.rpc.cluster.fault.circuitBreaker.CircuitBreakerProvider;
import com.lcx.rpc.cluster.fault.retry.RetryStrategy;
import com.lcx.rpc.cluster.fault.retry.RetryStrategyFactory;
import com.lcx.rpc.cluster.fault.tolerant.TolerantStrategy;
import com.lcx.rpc.cluster.fault.tolerant.TolerantStrategyFactory;
import com.lcx.rpc.cluster.loadbalancer.LoadBalancer;
import com.lcx.rpc.cluster.loadbalancer.LoadBalancerFactory;
import com.lcx.rpc.common.model.RpcRequest;
import com.lcx.rpc.common.model.RpcResponse;
import com.lcx.rpc.common.model.ServiceMetaInfo;
import com.lcx.rpc.cluster.register.Registry;
import com.lcx.rpc.cluster.register.RegistryFactory;
import com.lcx.rpc.transport.client.netty.NettyClient;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * jdk动态代理
 */
@Slf4j
public class ServiceProxy implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
        MyRpcConfig myRpcConfig = MyRpcApplication.getRpcConfig();
        String serviceName = method.getDeclaringClass().getName();

        // 构造请求
        RpcRequest rpcRequest = RpcRequest.builder()
                .interfaceName(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();

        // 服务发现
        ServiceMetaInfo serviceMetaInfo = ServiceMetaInfo.builder()
                .name(serviceName)
                .version(myRpcConfig.getVersion())
                .build();
        Registry registry = RegistryFactory.registry;
        Collection<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
        if (CollUtil.isEmpty(serviceMetaInfoList)) {
            // 打印日志
            log.info("未发现服务:{}", serviceMetaInfo.getServiceKey());
            throw new RuntimeException("未发现服务");
        }

        // 负载均衡
        LoadBalancer loadBalancer = LoadBalancerFactory.loadBalancer;
        Map<String, Object> params = Map.of("serviceKey", serviceMetaInfo.getServiceKey(), "host", myRpcConfig.getServer().getHost());
        final ServiceMetaInfo finalServiceMetaInfo = loadBalancer.select(params, new ArrayList<>(serviceMetaInfoList));

        RpcResponse response;
        CircuitBreaker circuitBreaker = CircuitBreakerProvider.getCircuitBreaker(finalServiceMetaInfo.getName());
        try {
            // 熔断判断
            if (!circuitBreaker.allowRequest()) throw new RuntimeException("熔断");

            if (finalServiceMetaInfo.getCanRetry()) { // 可重试
                RetryStrategy retryStrategy = RetryStrategyFactory.retryStrategy;
                response = retryStrategy.doRetry(() ->
                        NettyClient.doRequest(rpcRequest, finalServiceMetaInfo).get()
                );
            } else { // 不可重试
                response = NettyClient.doRequest(rpcRequest, finalServiceMetaInfo).get();
            }
            if (response.getCode() == 500) throw new RuntimeException();
            circuitBreaker.recordSuccess();
        } catch (Exception e) {
            circuitBreaker.recordFailure();
            // 容错机制
            TolerantStrategy tolerantStrategy = TolerantStrategyFactory.tolerantStrategy;
            response = tolerantStrategy.doTolerant(null, e);
        }
        return response.getData();
    }
}