package com.lcx.rpc.springboot.starter.bootstrap;

import com.lcx.rpc.bootstrap.config.MyRpcApplication;
import com.lcx.rpc.bootstrap.proxy.ServiceProxyFactory;
import com.lcx.rpc.springboot.starter.annotation.RpcReference;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;

/**
 * Rpc服务消费者启动类
 */
public class RpcConsumerBootStrap implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        for (Field field : beanClass.getDeclaredFields()) {
            RpcReference rpcReference = field.getAnnotation(RpcReference.class);
            if (rpcReference != null) {
                Class<?> interfaceClass = rpcReference.interfaceClass();
                if (interfaceClass == void.class) interfaceClass = field.getType();
                field.setAccessible(true);
                long globalTimeout = rpcReference.globalTimeout();
                if (globalTimeout == 0) globalTimeout = MyRpcApplication.getRpcConfig().getCluster().getRetry().getGlobalTimeout();
                Object proxy = ServiceProxyFactory.getProxy(interfaceClass, globalTimeout);
                try {
                    field.set(bean, proxy);
                    field.setAccessible(false);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("为字段注入代理对象失败", e);
                }
            }
        }
        return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
    }
}
