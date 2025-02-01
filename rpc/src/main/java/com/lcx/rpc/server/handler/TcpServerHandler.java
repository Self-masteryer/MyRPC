package com.lcx.rpc.server.handler;

import com.lcx.rpc.model.RpcRequest;
import com.lcx.rpc.model.RpcResponse;
import com.lcx.rpc.protocol.ProtocolMessage;
import com.lcx.rpc.protocol.ProtocolMessageDecoder;
import com.lcx.rpc.protocol.ProtocolMessageEncoder;
import com.lcx.rpc.protocol.enums.ProtocolMessageTypeEnum;
import com.lcx.rpc.register.LocalRegister;
import com.lcx.rpc.server.tcp.TcpBufferHandlerWrapper;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class TcpServerHandler implements Handler<NetSocket> {

    @Override
    public void handle(NetSocket netSocket) {
        netSocket.handler(new TcpBufferHandlerWrapper(buffer -> {
            ProtocolMessage<RpcRequest> requestProtocolMessage;
            try {
                requestProtocolMessage = (ProtocolMessage<RpcRequest>) ProtocolMessageDecoder.decode(buffer);
            } catch (IOException e) {
                throw new RuntimeException("协议消息解码错误");
            }

            RpcResponse response = new RpcResponse();
            try {
                doResponse(requestProtocolMessage.getBody(), response);
            } catch (Exception e) {
                e.printStackTrace();
                response.setMessage(e.getMessage());
                response.setException(e);
            }

            ProtocolMessage.Header header = requestProtocolMessage.getHeader();
            header.setType((byte) ProtocolMessageTypeEnum.RESPONSE.getValue());
            ProtocolMessage<RpcResponse> responseProtocolMessage = new ProtocolMessage<>(header, response);
            try {
                Buffer encode = ProtocolMessageEncoder.encode(responseProtocolMessage);
                netSocket.write(encode);
            } catch (Exception e) {
                throw new RuntimeException("协议消息编码错误");
            }
        }));
    }

    /**
     * 响应Rpc请求
     * @param request Rpc请求
     * @param response Rpc响应
     * @throws Exception 业务异常
     */
    protected void doResponse(RpcRequest request, RpcResponse response) throws Exception {
        Class<?> clazz = LocalRegister.get(request.getServiceName());
        Method method = clazz.getMethod(request.getMethodName(), request.getParameterTypes());
        method.setAccessible(true);
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object result = method.invoke(constructor.newInstance(), request.getArgs());
        response.setData(result);
        response.setDataType(method.getReturnType());
    }
}
