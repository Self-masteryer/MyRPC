package com.lcx.rpc.bootstrap;

import com.lcx.rpc.bootstrap.config.MyRpcApplication;
import com.lcx.rpc.bootstrap.config.MyRpcConfig;
import com.lcx.rpc.common.model.ServiceMetaInfo;
import com.lcx.rpc.common.model.ServiceRegisterInfo;
import com.lcx.rpc.cluster.register.LocalRegister;
import com.lcx.rpc.cluster.register.Registry;
import com.lcx.rpc.cluster.register.RegistryFactory;
import com.lcx.rpc.transport.server.handler.DefaultRpcReqHandler;
import com.lcx.rpc.transport.server.tcp.netty.NettyServer;

import java.util.List;

/**
 * 服务提供者启动类
 */
public class ProviderBootStrap {
    public static void init(List<ServiceRegisterInfo> serviceRegisterInfoList) {
        // Rpc框架初始化
        MyRpcApplication.init();
        // 全局配置
        final MyRpcConfig myRpcConfig = MyRpcApplication.getRpcConfig();
        Registry registry = RegistryFactory.registry;
        // 注册服务
        for (ServiceRegisterInfo serviceRegisterInfo : serviceRegisterInfoList) {
            String serviceName = serviceRegisterInfo.getServiceName();
            LocalRegister.register(serviceName, serviceRegisterInfo.getImplClass());
            // 注册服务到注册中心
            ServiceMetaInfo serviceMetaInfo = ServiceMetaInfo.builder()
                    .name(serviceName)
                    .version(myRpcConfig.getVersion())
                    .host(myRpcConfig.getServer().getHost())
                    .port(myRpcConfig.getServer().getPort())
                    .weight(myRpcConfig.getWeight())
                    .canRetry(serviceRegisterInfo.getCanRetry())
                    .build();
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