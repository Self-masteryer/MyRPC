package com.lcx.rpc.server.http;

import com.lcx.rpc.server.RpcServer;
import com.lcx.rpc.server.handler.RpcReqHandler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VertxHttpServer implements RpcServer {

    @Override
    public void doStart(int port, RpcReqHandler reqHandler) {
        Vertx vertx = Vertx.vertx();
        HttpServer httpServer = vertx.createHttpServer();
        httpServer.requestHandler(new VertxHttpServerHandler());
        httpServer.listen(port, res -> {
            if (res.succeeded()) {
                log.info("HttpServer started on port {}", port);
            } else {
                log.info("Failed to start HttpServer: {}", res.cause().getMessage());
            }
        });
    }
}