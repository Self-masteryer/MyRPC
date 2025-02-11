package com.lcx.rpc.server.handler;

import java.lang.reflect.Constructor;

/**
 * new出实例对象
 */
public class NewRpcReqHandler implements RpcReqHandler{

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
