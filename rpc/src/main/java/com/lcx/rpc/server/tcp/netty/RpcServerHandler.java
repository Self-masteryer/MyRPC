package com.lcx.rpc.server.tcp.netty;

import com.lcx.rpc.model.RpcRequest;
import com.lcx.rpc.model.RpcResponse;
import com.lcx.rpc.protocol.ProtocolMessage;
import com.lcx.rpc.protocol.enums.ProtocolMessageTypeEnum;
import com.lcx.rpc.server.handler.RpcReqHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * RPC服务端处理器
 */
@Slf4j
public class RpcServerHandler extends SimpleChannelInboundHandler<ProtocolMessage<RpcRequest>> {

    private final RpcReqHandler rpcReqHandler;

    public RpcServerHandler(RpcReqHandler rpcReqHandler) {
        this.rpcReqHandler = rpcReqHandler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtocolMessage<RpcRequest> protocolMessage) throws Exception {
        RpcResponse response = new RpcResponse();
        try {
            rpcReqHandler.doResponse(protocolMessage.getBody(), response);
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
}
