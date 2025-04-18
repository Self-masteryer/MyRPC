package com.lcx.rpc.transport.server.handler;

import com.lcx.rpc.cluster.fault.restrictor.provider.RestrictorProvider;
import com.lcx.rpc.cluster.fault.retry.DeadlineThreadContext;
import com.lcx.rpc.common.exception.BusinessException;
import com.lcx.rpc.common.exception.RetryableException;
import com.lcx.rpc.common.model.RpcRequest;
import com.lcx.rpc.common.model.RpcResponse;
import com.lcx.rpc.cluster.register.LocalRegister;

import java.lang.reflect.InvocationTargetException;
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
            DeadlineThreadContext.setDeadline(request.getDeadline());
            Class<?> clazz = LocalRegister.get(request.getInterfaceName());

            String serviceName = clazz.getName();
            // 令牌桶限流
            boolean token = RestrictorProvider.getRateLimit(serviceName).getToken();
            if (!token) return RpcResponse.retryable("请求过于频繁，请稍后重试");

            Method method = clazz.getMethod(request.getMethodName(), request.getParameterTypes());
            method.setAccessible(true);
            Object instance = getInstance(clazz);
            Object result = method.invoke(instance, request.getArgs());
            return RpcResponse.success(result);
        } catch (InvocationTargetException e) { // 反射执行异常
            Throwable cause = e.getCause();
            if (cause instanceof BusinessException business) { // 业务异常
                return RpcResponse.fail(business.getErrorCode(), cause.getMessage());
            }
            if (cause instanceof RetryableException retry) { // 系统可重试异常
                return RpcResponse.retryable(retry.getMessage());
            }
            return RpcResponse.fail(500, cause.getMessage()); // 系统不可重试异常
        } catch (NoSuchMethodException | IllegalAccessException e) { // 反射异常
            return RpcResponse.fail(500, e.getMessage());
        } finally {
            DeadlineThreadContext.removeDeadline();
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
