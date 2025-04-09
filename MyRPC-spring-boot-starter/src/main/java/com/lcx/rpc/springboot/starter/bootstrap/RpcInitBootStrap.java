package com.lcx.rpc.springboot.starter.bootstrap;

import com.lcx.rpc.bootstrap.config.MyRpcApplication;
import com.lcx.rpc.transport.server.tcp.netty.NettyServer;
import com.lcx.rpc.springboot.starter.annotation.EnableRpc;
import com.lcx.rpc.springboot.starter.server.SpringRpcReqHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Rpc框架初始化
 */
@Slf4j
public class RpcInitBootStrap implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // RPC框架初始化
        MyRpcApplication.init();

        String enableRpcName = EnableRpc.class.getName();
        if ((boolean) importingClassMetadata.getAnnotationAttributes(enableRpcName).get("needServer")) {
            // 启动服务器
            new NettyServer(new SpringRpcReqHandler()).start(MyRpcApplication.getRpcConfig().getServer().getPort());
        } else {
            log.info("RPC Server is not started");
        }
    }
}
