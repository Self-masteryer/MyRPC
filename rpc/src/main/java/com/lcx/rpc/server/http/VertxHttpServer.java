package com.lcx.rpc.server.http;

import com.lcx.rpc.server.RpcServer;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VertxHttpServer implements RpcServer<HttpServerRequest> {

    @Override
    public void doStart(int port, Handler<HttpServerRequest> handler) {
        Vertx vertx = Vertx.vertx();
        HttpServer httpServer = vertx.createHttpServer();
        httpServer.requestHandler(handler);
        httpServer.listen(port, res -> {
            if (res.succeeded()) {
                log.info("HttpServer started on port {}", port);
            } else {
                log.info("Failed to start HttpServer: {}", res.cause().getMessage());
            }
        });
    }
}