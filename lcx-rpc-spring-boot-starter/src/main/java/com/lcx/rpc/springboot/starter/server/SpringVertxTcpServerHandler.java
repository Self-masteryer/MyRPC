package com.lcx.rpc.springboot.starter.server;

import com.lcx.rpc.model.RpcRequest;
import com.lcx.rpc.model.RpcResponse;
import com.lcx.rpc.register.LocalRegister;
import com.lcx.rpc.server.tcp.vertx.VertxTcpServerHandler;
import com.lcx.rpc.springboot.starter.utils.SpringContextUtil;

import java.lang.reflect.Method;

/**
 * 集成Spring的Tcp服务处理器
 */
public class SpringVertxTcpServerHandler extends VertxTcpServerHandler {

    @Override
    protected void doResponse(RpcRequest request, RpcResponse response) throws Exception {
        Class<?> clazz = LocalRegister.get(request.getServiceName());
        Object service = SpringContextUtil.getBean(clazz);
        Method method = clazz.getMethod(request.getMethodName(), request.getParameterTypes());
        method.setAccessible(true);
        Object result = method.invoke(service, request.getArgs());
        response.setData(result);
        response.setDataType(method.getReturnType());
    }

}
