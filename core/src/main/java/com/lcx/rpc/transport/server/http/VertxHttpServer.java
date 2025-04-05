package com.lcx.rpc.transport.server.http;

import com.lcx.rpc.transport.server.RpcServer;
import com.lcx.rpc.transport.server.handler.RpcReqHandler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VertxHttpServer implements RpcServer {

    private final RpcReqHandler rpcReqHandler;

    public VertxHttpServer(RpcReqHandler rpcReqHandler) {
        this.rpcReqHandler = rpcReqHandler;
    }

    @Override
    public void start(int port) {
        Vertx vertx = Vertx.vertx();
        HttpServer httpServer = vertx.createHttpServer();
        httpServer.requestHandler(new VertxHttpServerHandler(rpcReqHandler));
        httpServer.listen(port, res -> {
            if (res.succeeded()) {
                log.info("HttpServer started on port {}", port);
            } else {
                log.info("Failed to start HttpServer: {}", res.cause().getMessage());
            }
        });
    }
}