package com.lcx.rpc.transport.server.handler;

import com.lcx.rpc.cluster.fault.restrictor.provider.RestrictorProvider;
import com.lcx.rpc.common.model.RpcRequest;
import com.lcx.rpc.common.model.RpcResponse;
import com.lcx.rpc.cluster.register.LocalRegister;

import java.lang.reflect.Method;

/**
 * Rpc请求处理器
 */
public interface RpcReqHandler {

    /**
     * 响应Rpc请求
     *
     * @param request Rpc请求
     */
    default RpcResponse doResponse(RpcRequest request) {
        try {
            Class<?> clazz = LocalRegister.get(request.getInterfaceName());

            String serviceName = clazz.getName();
            boolean token = RestrictorProvider.getRateLimit(serviceName).getToken();
            if(!token) return RpcResponse.fail();

            Method method = clazz.getMethod(request.getMethodName(), request.getParameterTypes());
            method.setAccessible(true);
            Object instance = getInstance(clazz);
            Object result = method.invoke(instance, request.getArgs());
            return RpcResponse.success(result);
        } catch (Exception e) { // 执行过程中发生异常
            // todo 是否传回异常
            return RpcResponse.fail();
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
