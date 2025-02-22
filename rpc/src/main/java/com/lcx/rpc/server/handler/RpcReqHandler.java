package com.lcx.rpc.server.handler;

import com.lcx.rpc.model.RpcRequest;
import com.lcx.rpc.model.RpcResponse;
import com.lcx.rpc.register.LocalRegister;

import java.lang.reflect.Method;

/**
 * Rpc请求处理器
 */
public interface RpcReqHandler {

    /**
     * 响应Rpc请求
     *
     * @param request  Rpc请求
     * @param response Rpc响应
     */
    default void doResponse(RpcRequest request, RpcResponse response) {
        try {
            Class<?> clazz = LocalRegister.get(request.getServiceName());
            Method method = clazz.getMethod(request.getMethodName(), request.getParameterTypes());
            method.setAccessible(true);
            Object instance = getInstance(clazz);
            Object result = method.invoke(instance, request.getArgs());
            response.setData(result);
            response.setDataType(method.getReturnType());
        } catch (Exception e) {
            e.printStackTrace();
            Throwable cause = e.getCause();
            response.setMessage(cause.getMessage());
            response.setException(e);
        }
    }

    /**
     * 获取服务实例对象
     *
     * @param clazz 服务类字节码对象
     * @return 服务实例对象
     */
    Object getInstance(Class<?> clazz);
}
