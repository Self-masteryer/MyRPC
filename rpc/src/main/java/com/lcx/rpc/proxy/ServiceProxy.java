package com.lcx.rpc.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.poi.excel.sax.handler.BeanRowHandler;
import com.esotericsoftware.minlog.Log;
import com.lcx.rpc.config.RpcApplication;
import com.lcx.rpc.constant.RpcConstant;
import com.lcx.rpc.model.RpcRequest;
import com.lcx.rpc.model.RpcResponse;
import com.lcx.rpc.model.ServiceMetaInfo;
import com.lcx.rpc.protocol.ProtocolConstant;
import com.lcx.rpc.protocol.ProtocolMessage;
import com.lcx.rpc.protocol.ProtocolMessageDecoder;
import com.lcx.rpc.protocol.ProtocolMessageEncoder;
import com.lcx.rpc.protocol.enums.ProtocolMessageSerializerEnum;
import com.lcx.rpc.protocol.enums.ProtocolMessageTypeEnum;
import com.lcx.rpc.register.Registry;
import com.lcx.rpc.register.RegistryFactory;
import com.lcx.rpc.serializer.Serializer;
import com.lcx.rpc.serializer.SerializerFactory;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * jdk动态代理
 */
public class ServiceProxy implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String servicePath = method.getDeclaringClass().getName();
        String serviceName = servicePath.substring(servicePath.lastIndexOf(".") + 1);
        // 构造请求
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();
        // 服务发现
        ServiceMetaInfo serviceMetaInfo = ServiceMetaInfo.builder()
                .name(serviceName)
                .version(RpcApplication.getRpcConfig().getVersion())
                .build();
        Registry registry = RegistryFactory.registry;
        Collection<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
        if (CollUtil.isEmpty(serviceMetaInfoList)) {
            throw new RuntimeException("未发现服务");
        }
        serviceMetaInfo = serviceMetaInfoList.iterator().next();

        // 基于tcp发送自定义rpc请求
        Vertx vertx = Vertx.vertx();
        // 创建tcp客户端
        NetClient client = vertx.createNetClient();
        CompletableFuture<RpcResponse> cf = new CompletableFuture<>();
        // 建立tcp连接
        client.connect(serviceMetaInfo.getPort(), serviceMetaInfo.getHost(), result -> {
            if (result.succeeded()) { // 发送rpc请求
                Log.debug("TCP server连接成功");
                NetSocket socket = result.result();
                // 构建rpc请求
                ProtocolMessage.Header header = ProtocolMessage.Header.builder()
                        .magic(ProtocolConstant.PROTOCOL_MAGIC)
                        .version(ProtocolConstant.PROTOCOL_VERSION)
                        .serializer((byte) Objects.requireNonNull(ProtocolMessageSerializerEnum
                                .getByValue(RpcApplication.getRpcConfig().getSerializer()))
                                .getKey())
                        .type((byte) ProtocolMessageTypeEnum.REQUEST.getValue())
                        .requestId(IdUtil.getSnowflakeNextId())
                        .build();
                ProtocolMessage<RpcRequest> requestProtocolMessage = new ProtocolMessage<>(header, rpcRequest);

                try {
                    Buffer encode = ProtocolMessageEncoder.encode(requestProtocolMessage);
                    socket.write(encode);
                } catch (IOException e) {
                    throw new RuntimeException("协议消息编码错误");
                }
                socket.handler(buffer -> {
                    try {
                        var decode = (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decode(buffer);
                        cf.complete(decode.getBody());
                    } catch (IOException e) {
                        throw new RuntimeException("协议消息译码错误");
                    }
                });
            } else {
                Log.error("TCP server连接失败");
            }
        });
        RpcResponse response = cf.get();
        client.close();
        return response.getData();
//        // 基于http发送rpc请求
//        HttpResponse httpResponse = HttpRequest.post(serviceMetaInfo.getServiceAddress())
//                .body(bodyBytes)
//                .execute();
//        byte[] result = httpResponse.bodyBytes();
//        // 反序列化
//        RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
//        return rpcResponse.getData();
    }
}