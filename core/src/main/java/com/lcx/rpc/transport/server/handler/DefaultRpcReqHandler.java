package com.lcx.rpc.transport.server.handler;

import java.lang.reflect.Constructor;

/**
 * 默认Rpc请求处理器：new出实例对象（测试使用）
 */
public class DefaultRpcReqHandler implements RpcReqHandler{

    @Override
    public Object getInstance(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
