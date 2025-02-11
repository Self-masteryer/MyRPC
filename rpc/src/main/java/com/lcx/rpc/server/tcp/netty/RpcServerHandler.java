package com.lcx.rpc.server.tcp.netty;

import com.lcx.rpc.model.RpcRequest;
import com.lcx.rpc.model.RpcResponse;
import com.lcx.rpc.protocol.ProtocolMessage;
import com.lcx.rpc.protocol.enums.ProtocolMessageTypeEnum;
import com.lcx.rpc.register.LocalRegister;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * RPC服务端处理器
 */
@Slf4j
public class RpcServerHandler extends SimpleChannelInboundHandler<ProtocolMessage<RpcRequest>> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtocolMessage<RpcRequest> protocolMessage) throws Exception {
        RpcResponse response = new RpcResponse();
        try {
            doResponse(protocolMessage.getBody(), response);
        } catch (Exception e) {
            e.printStackTrace();
            Throwable cause = e.getCause();
            response.setMessage(cause.getMessage());
            response.setException(e);
        }
        ProtocolMessage.Header header = protocolMessage.getHeader();
        header.setMessageType((byte) ProtocolMessageTypeEnum.RESPONSE.getValue());
        ProtocolMessage<RpcResponse> responseProtocolMessage = new ProtocolMessage<>(header, response);
        ctx.writeAndFlush(responseProtocolMessage);
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
