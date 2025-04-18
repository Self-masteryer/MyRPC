package com.lcx.rpc.bootstrap;

import com.lcx.rpc.bootstrap.config.MyRpcApplication;
import com.lcx.rpc.bootstrap.config.MyRpcConfig;
import com.lcx.rpc.cluster.fault.retry.Retryable;
import com.lcx.rpc.common.model.ServiceMetaInfo;
import com.lcx.rpc.common.model.ServiceRegisterInfo;
import com.lcx.rpc.cluster.register.LocalRegister;
import com.lcx.rpc.cluster.register.Registry;
import com.lcx.rpc.cluster.register.RegistryFactory;
import com.lcx.rpc.transport.server.handler.DefaultRpcReqHandler;
import com.lcx.rpc.transport.server.tcp.netty.NettyServer;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务提供者启动类
 */
public class ProviderBootStrap {

    public static void init(List<ServiceRegisterInfo> serviceRegisterList) {
        // MyRPC框架初始化
        MyRpcApplication.init();
        // 全局配置
        final MyRpcConfig myRpcConfig = MyRpcApplication.getRpcConfig();
        Registry registry = RegistryFactory.registry;
        // 注册服务
        for (ServiceRegisterInfo serviceRegisterInfo : serviceRegisterList) {
            String serviceName = serviceRegisterInfo.getServiceName();
            Class<?> serviceClass = serviceRegisterInfo.getImplClass();
            LocalRegister.register(serviceName, serviceClass);
            // 注册服务到注册中心
            ServiceMetaInfo serviceMetaInfo = ServiceMetaInfo.builder()
                    .name(serviceName)
                    .version(myRpcConfig.getVersion())
                    .host(myRpcConfig.getServer().getHost())
                    .port(myRpcConfig.getServer().getPort())
                    .weight(myRpcConfig.getWeight())
                    .build();

            // 扫描可重试注解
            if (serviceClass.isAnnotationPresent(Retryable.class)) {
                serviceMetaInfo.setIdempotent(true);
            } else {
                Map<String, Boolean> idempotentMap = new HashMap<>();
                Method[] methods = serviceClass.getDeclaredMethods();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(Retryable.class)) {
                        idempotentMap.put(method.getName(), true);
                    }
                }
                serviceMetaInfo.setIdempotentMap(idempotentMap);
            }
            try {
                registry.register(serviceMetaInfo);
            } catch (Exception e) {
                throw new RuntimeException(serviceName + " 服务注册失败", e);
            }
        }
        // 启动服务器
        new NettyServer(new DefaultRpcReqHandler()).start(8081);
    }

}