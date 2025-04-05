package com.lcx.rpc.bootstrap.proxy;

import java.lang.reflect.Proxy;

/**
 * 服务代理工厂
 */
public class ServiceProxyFactory {
    /**
     * 根据服务类创建代理对象
     */
    public static <T> T getProxy(Class<T> clazz) {
        return clazz.cast(Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class[]{clazz},
                new ServiceProxy())
        );
    }
}