package com.lcx.rpc.bootstrap;

import com.lcx.rpc.config.RpcApplication;
import com.lcx.rpc.config.RpcConfig;
import com.lcx.rpc.model.ServiceMetaInfo;
import com.lcx.rpc.model.ServiceRegisterInfo;
import com.lcx.rpc.register.LocalRegister;
import com.lcx.rpc.register.Registry;
import com.lcx.rpc.register.RegistryFactory;
import com.lcx.rpc.server.handler.NewRpcReqHandler;
import com.lcx.rpc.server.tcp.netty.NettyServer;

import java.util.List;

/**
 * 服务提供者启动类
 */
public class ProviderBootStrap {
    public static void init(List<ServiceRegisterInfo> serviceRegisterInfoList) {
        // Rpc框架初始化
        RpcApplication.init();
        // 全局配置
        final RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        Registry registry = RegistryFactory.registry;
        // 注册服务
        for (ServiceRegisterInfo<?> serviceRegisterInfo : serviceRegisterInfoList) {
            String serviceName = serviceRegisterInfo.getServiceName();
            LocalRegister.register(serviceName, serviceRegisterInfo.getImplClass());
            // 注册服务到注册中心
            ServiceMetaInfo serviceMetaInfo = ServiceMetaInfo.builder()
                    .name(serviceName)
                    .version(rpcConfig.getVersion())
                    .host(rpcConfig.getHost())
                    .port(rpcConfig.getPort())
                    .weight(rpcConfig.getWeight())
                    .build();
            try {
                registry.register(serviceMetaInfo);
            } catch (Exception e) {
                throw new RuntimeException(serviceName + " 服务注册失败", e);
            }
        }
        // 启动服务器
        new NettyServer().doStart(8081,new NewRpcReqHandler());
    }
}