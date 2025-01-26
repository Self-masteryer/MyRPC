package com.lcx.rpc.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.lcx.rpc.config.RpcApplication;
import com.lcx.rpc.constant.RpcConstant;
import com.lcx.rpc.model.RpcRequest;
import com.lcx.rpc.model.RpcResponse;
import com.lcx.rpc.model.ServiceMetaInfo;
import com.lcx.rpc.register.Registry;
import com.lcx.rpc.register.RegistryFactory;
import com.lcx.rpc.serializer.Serializer;
import com.lcx.rpc.serializer.SerializerFactory;

import java.io.IOException;
import java.util.Collection;

/**
 * jdk动态代理
 */
public class ServiceProxy implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 获取配置的序列化器
        Serializer serializer = SerializerFactory.serializer;
        String servicePath = method.getDeclaringClass().getName();
        String serviceName = servicePath.substring(servicePath.lastIndexOf(".") + 1);
        // 构造请求
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();
        try {
            // 序列化
            byte[] bodyBytes = serializer.serialize(rpcRequest);
            // 服务发现
            ServiceMetaInfo serviceMetaInfo = ServiceMetaInfo.builder()
                    .name(serviceName)
                    .version(RpcApplication.getRpcConfig().getVersion())
                    .build();
            Registry registry = RegistryFactory.registry;
            Collection<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
            if (CollUtil.isEmpty(serviceMetaInfoList)) {
                throw new RuntimeException("未发现服务");
            }
            serviceMetaInfo = serviceMetaInfoList.iterator().next();
            // 发送请求
            try (HttpResponse httpResponse = HttpRequest.post(serviceMetaInfo.getServiceAddress())
                    .body(bodyBytes)
                    .execute()) {
                byte[] result = httpResponse.bodyBytes();
                // 反序列化
                RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
                return rpcResponse.getData();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
