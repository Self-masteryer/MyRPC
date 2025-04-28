package com.lcx.rpc.springboot.starter.bootstrap;

import com.lcx.rpc.bootstrap.config.MyRpcApplication;
import com.lcx.rpc.bootstrap.config.MyRpcConfig;
import com.lcx.rpc.cluster.fault.retry.Retryable;
import com.lcx.rpc.common.model.ServiceMetaInfo;
import com.lcx.rpc.cluster.register.LocalRegister;
import com.lcx.rpc.cluster.register.Registry;
import com.lcx.rpc.cluster.register.RegistryFactory;
import com.lcx.rpc.springboot.starter.annotation.RpcService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Rpc服务提供者启动类
 */
public class RpcProviderBootStrap implements BeanPostProcessor {
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        RpcService rpcService = beanClass.getAnnotation(RpcService.class);
        if (rpcService != null) {
            Class<?> interfaceClass = rpcService.interfaceClass();
            if (interfaceClass == void.class) {
                interfaceClass = beanClass.getInterfaces()[0];
            }

            String serviceName = interfaceClass.getName();
            String serviceVersion = rpcService.serviceVersion();
            // 本地服务注册: 接口名称,实现类
            LocalRegister.register(serviceName, beanClass);
            // 服务注册
            MyRpcConfig rpcConfig = MyRpcApplication.getRpcConfig();
            Registry registry = RegistryFactory.registry;

            ServiceMetaInfo serviceMetaInfo = ServiceMetaInfo.builder()
                    .name(serviceName)
                    .version(serviceVersion)
                    .host(rpcConfig.getServer().getHost())
                    .port(rpcConfig.getServer().getPort())
                    .weight(rpcConfig.getWeight())
                    .build();

            // 扫描可重试注解
            if (interfaceClass.isAnnotationPresent(Retryable.class)) {
                serviceMetaInfo.setIdempotent(true);
            } else {
                Map<String, Boolean> idempotentMap = new HashMap<>();
                Method[] methods = interfaceClass.getDeclaredMethods();
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
                throw new RuntimeException(serviceName + "服务注册失败", e);
            }

        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
