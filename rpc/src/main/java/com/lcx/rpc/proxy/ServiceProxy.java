package com.lcx.rpc.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import cn.hutool.core.collection.CollUtil;
import com.lcx.rpc.config.RpcApplication;
import com.lcx.rpc.config.RpcConfig;
import com.lcx.rpc.fault.retry.RetryStrategy;
import com.lcx.rpc.fault.retry.RetryStrategyFactory;
import com.lcx.rpc.fault.tolerant.TolerantStrategy;
import com.lcx.rpc.fault.tolerant.TolerantStrategyFactory;
import com.lcx.rpc.loadbalancer.LoadBalancer;
import com.lcx.rpc.loadbalancer.LoadBalancerFactory;
import com.lcx.rpc.model.RpcRequest;
import com.lcx.rpc.model.RpcResponse;
import com.lcx.rpc.model.ServiceMetaInfo;
import com.lcx.rpc.register.Registry;
import com.lcx.rpc.register.RegistryFactory;
import com.lcx.rpc.server.tcp.VertxTcpClient;
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
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        String serviceName = method.getDeclaringClass().getName();

        // 构造请求
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();

        // 服务发现
        ServiceMetaInfo serviceMetaInfo = ServiceMetaInfo.builder()
                .name(serviceName)
                .version(rpcConfig.getVersion())
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
        final ServiceMetaInfo finalServiceMetaInfo = loadBalancer.select(Map.of("host", rpcConfig.getHost()), new ArrayList<>(serviceMetaInfoList));

        // 重试
        RetryStrategy retryStrategy = RetryStrategyFactory.retryStrategy;
        RpcResponse response = null;
        try {
            response = retryStrategy.doRetry(() ->
                    VertxTcpClient.doRequest(rpcRequest, finalServiceMetaInfo)
            );
        } catch (Exception e) {
            // 容错机制
            TolerantStrategy tolerantStrategy = TolerantStrategyFactory.tolerantStrategy;
            tolerantStrategy.doTolerant(null, e);
        }

        assert response != null : "response is null";
        Exception e = response.getException();
        if (e != null) throw new RuntimeException(response.getMessage(), e);

        return response.getData();
//        // 基于http发送rpc请求
//        HttpResponse httpResponse = HttpRequest.post(serviceMetaInfo.getServiceAddress())
//                .body(bodyBytes)
//                .execute();
//        byte[] result = httpResponse.bodyBytes();
//        // 反序列化
//        RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
//        return rpcResponse.getData();
    }
}