package com.lcx.rpc.transport.server.http;

import com.lcx.rpc.bootstrap.config.MyRpcApplication;
import com.lcx.rpc.common.model.RpcRequest;
import com.lcx.rpc.common.model.RpcResponse;
import com.lcx.rpc.protocol.serializer.Serializer;
import com.lcx.rpc.protocol.serializer.SerializerFactory;

import com.lcx.rpc.transport.server.handler.RpcReqHandler;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class VertxHttpServerHandler implements Handler<HttpServerRequest> {

    private final RpcReqHandler rpcReqHandler;

    public VertxHttpServerHandler(RpcReqHandler rpcReqHandler) {
        this.rpcReqHandler = rpcReqHandler;
    }

    @Override
    public void handle(HttpServerRequest request) {
        final Serializer serializer = SerializerFactory.getSerializer(MyRpcApplication.getRpcConfig().getProtocol().getSerializer());
        log.info("Receive request:{} {}", request.method(), request.uri());
        request.bodyHandler(body -> {
            byte[] bytes = body.getBytes();
            RpcRequest rpcRequest;
            try {
                rpcRequest = serializer.deserialize(bytes, RpcRequest.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            RpcResponse rpcResponse = rpcReqHandler.doResponse(rpcRequest);
            doResponse(request, rpcResponse, serializer);
        });

    }

    private void doResponse(HttpServerRequest request, RpcResponse rpcResponse, Serializer serializer) {
        HttpServerResponse response = request.response();
        response.putHeader("content-type", "application/json");
        try {
            byte[] serialized = serializer.serialize(rpcResponse);
            response.end(Buffer.buffer(serialized));
        } catch (IOException e) {
            e.printStackTrace();
            response.end(Buffer.buffer());
        }
    }
}
