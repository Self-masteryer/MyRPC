package com.lcx.rpc.protocol.serializer.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lcx.rpc.common.model.RpcRequest;
import com.lcx.rpc.common.model.RpcResponse;
import com.lcx.rpc.protocol.serializer.Serializer;

import java.io.IOException;

/**
 * json序列化方式
 */
public class JsonSerializer implements Serializer {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public <T> byte[] serialize(T object) throws IOException {
        return OBJECT_MAPPER.writeValueAsBytes(object);
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws IOException {
        T obj = OBJECT_MAPPER.readValue(data, clazz);
        if (obj instanceof RpcRequest) {
            return handleRequest((RpcRequest) obj, clazz);
        } else if (obj instanceof RpcResponse) {
            return handleResponse((RpcResponse) obj, clazz);
        } else {
            return obj;
        }
    }

    /**
     * 由于Object的原始对象会被擦除，导致反序列化时会被作为 LinkedHashMap 无法转换成原始对象，因此这里做了特殊处理
     *
     * @param rpcRequest rpc请求
     * @param type       类型
     * @return {@link T}
     * @throws IOException IO异常
     */
    private <T> T handleRequest(RpcRequest rpcRequest, Class<T> type) throws IOException {
        Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        Object[] args = rpcRequest.getArgs();

        // 循环处理每个参数的类型
        for (int i = 0; i < parameterTypes.length; i++) {
            if (args[i] == null) continue;
            Class<?> clazz = parameterTypes[i];
            // 如果类型不同，则重新处理一下类型
            if (!clazz.isAssignableFrom(args[i].getClass())) {
                byte[] argBytes = OBJECT_MAPPER.writeValueAsBytes(args[i]);
                args[i] = OBJECT_MAPPER.readValue(argBytes, clazz);
            }
        }
        return type.cast(rpcRequest);
    }

    /**
     * 由于Object的原始对象会被擦除，导致反序列化时会被作为 LinkedHashMap 无法转换成原始对象，因此这里做了特殊处理
     *
     * @param rpcResponse rpc 响应
     * @param type        类型
     * @return {@link T}
     * @throws IOException IO异常
     */
    private <T> T handleResponse(RpcResponse rpcResponse, Class<T> type) throws IOException {
        Object data = rpcResponse.getData();
        if (data != null) {
            // 处理响应数据
            byte[] dataBytes = OBJECT_MAPPER.writeValueAsBytes(data);
            rpcResponse.setData(OBJECT_MAPPER.readValue(dataBytes, rpcResponse.getDataType()));
        }
        return type.cast(rpcResponse);
    }
}
