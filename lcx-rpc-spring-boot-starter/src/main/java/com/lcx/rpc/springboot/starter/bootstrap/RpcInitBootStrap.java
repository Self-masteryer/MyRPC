package com.lcx.rpc.springboot.starter.bootstrap;

import com.lcx.rpc.config.RpcApplication;
import com.lcx.rpc.server.tcp.VertxTcpServer;
import com.lcx.rpc.springboot.starter.annotation.EnableRpc;
import com.lcx.rpc.springboot.starter.server.SpringTcpServerHandler;
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
        RpcApplication.init();

        String enableRpcName = EnableRpc.class.getName();
        if ((boolean) importingClassMetadata.getAnnotationAttributes(enableRpcName).get("needServer")) {
            // 启动服务器
            new VertxTcpServer().doStart(RpcApplication.getRpcConfig().getPort(), new SpringTcpServerHandler());
        } else {
            log.info("RPC Server is not started");
        }
    }
}
