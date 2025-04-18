package com.lcx.rpc.bootstrap.proxy;

import java.lang.reflect.Proxy;

/**
 * 服务代理工厂
 */
public class ServiceProxyFactory {

    /**
     * 根据服务类创建代理对象
     * @param clazz 被代理类字节码对象
     * @param timeout 超时时间
     * @return 代理
     * @param <T> 被代理类
     */
    public static <T> T getProxy(Class<T> clazz, long timeout) {
        return clazz.cast(Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class[]{clazz},
                new ServiceProxy(timeout))
        );
    }
}