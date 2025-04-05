package com.lcx.rpc.transport.server.tcp.vertx;

import com.lcx.rpc.common.model.RpcRequest;
import com.lcx.rpc.common.model.RpcResponse;
import com.lcx.rpc.protocol.ProtocolMessage;
import com.lcx.rpc.protocol.enums.ProtocolMessageTypeEnum;
import com.lcx.rpc.transport.server.handler.RpcReqHandler;
import com.lcx.rpc.transport.server.tcp.vertx.codec.ProtocolMessageDecoder;
import com.lcx.rpc.transport.server.tcp.vertx.codec.ProtocolMessageEncoder;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

import java.io.IOException;

public class VertxTcpServerHandler implements Handler<NetSocket> {

    private final RpcReqHandler rpcReqHandler;

    public VertxTcpServerHandler(RpcReqHandler rpcReqHandler) {
        this.rpcReqHandler = rpcReqHandler;
    }

    @Override
    public void handle(NetSocket netSocket) {
        netSocket.handler(new TcpBufferHandlerWrapper(buffer -> {
            ProtocolMessage<RpcRequest> requestProtocolMessage;
            try {
                requestProtocolMessage = (ProtocolMessage<RpcRequest>) ProtocolMessageDecoder.decode(buffer);
            } catch (IOException e) {
                throw new RuntimeException("协议消息解码错误");
            }
            RpcResponse response = rpcReqHandler.doResponse(requestProtocolMessage.getBody());
            ProtocolMessage.Header header = requestProtocolMessage.getHeader();
            header.setMessageType((byte) ProtocolMessageTypeEnum.RESPONSE.getValue());
            ProtocolMessage<RpcResponse> responseProtocolMessage = new ProtocolMessage<>(header, response);
            try {
                Buffer encode = ProtocolMessageEncoder.encode(responseProtocolMessage);
                netSocket.write(encode);
            } catch (Exception e) {
                throw new RuntimeException("协议消息编码错误");
            }
        }));
    }
}
