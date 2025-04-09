package com.lcx.rpc.transport.server.tcp.netty.handler;

import com.lcx.rpc.common.model.RpcPing;
import com.lcx.rpc.common.model.RpcRequest;
import com.lcx.rpc.common.model.RpcResponse;
import com.lcx.rpc.protocol.ProtocolMessage;
import com.lcx.rpc.protocol.enums.ProtocolMessageTypeEnum;
import com.lcx.rpc.transport.server.handler.RpcReqHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * RPC服务端处理器
 */
@Slf4j
public class RpcServerHandler extends SimpleChannelInboundHandler<ProtocolMessage<?>> {

    private final RpcReqHandler rpcReqHandler;

    public RpcServerHandler(RpcReqHandler rpcReqHandler) {
        this.rpcReqHandler = rpcReqHandler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtocolMessage<?> protocolMessage) throws Exception {
        ProtocolMessage<RpcResponse> responseProtocolMessage = null;
        Object body = protocolMessage.getBody();
        if (body instanceof RpcRequest rpcRequest) {
            RpcResponse response = rpcReqHandler.doResponse(rpcRequest);
            ProtocolMessage.Header header = protocolMessage.getHeader();
            header.setMessageType((byte) ProtocolMessageTypeEnum.RESPONSE.getValue());
            responseProtocolMessage = new ProtocolMessage<>(header, response);
            ctx.writeAndFlush(responseProtocolMessage);
        } else if (body instanceof RpcPing rpcPing) {
            log.info("接收到来自客户端的心跳包");
        }
    }

}
