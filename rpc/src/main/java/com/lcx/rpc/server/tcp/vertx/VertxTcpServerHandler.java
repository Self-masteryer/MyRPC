package com.lcx.rpc.server.tcp.vertx;

import com.lcx.rpc.model.RpcRequest;
import com.lcx.rpc.model.RpcResponse;
import com.lcx.rpc.protocol.ProtocolMessage;
import com.lcx.rpc.protocol.ProtocolMessageDecoder;
import com.lcx.rpc.protocol.ProtocolMessageEncoder;
import com.lcx.rpc.protocol.enums.ProtocolMessageTypeEnum;
import com.lcx.rpc.server.handler.RpcReqHandler;
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

            RpcResponse response = new RpcResponse();
            try {
                rpcReqHandler.doResponse(requestProtocolMessage.getBody(), response);
            } catch (Exception e) {
                e.printStackTrace();
                Throwable cause = e.getCause();
                response.setMessage(cause.getMessage());
                response.setException(e);
            }

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
