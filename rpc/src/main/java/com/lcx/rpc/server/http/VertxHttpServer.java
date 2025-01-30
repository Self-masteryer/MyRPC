package com.lcx.rpc.server.http;

import com.lcx.rpc.server.RpcServer;
import com.lcx.rpc.server.handler.HttpServerHandler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VertxHttpServer implements RpcServer {

    private static final Logger log = LoggerFactory.getLogger(VertxHttpServer.class);

    @Override
    public void doStart(int port) {
        // 创建vert.x实例
        Vertx vertx = Vertx.vertx();
        // 创建http服务器
        HttpServer httpServer = vertx.createHttpServer();
        // 设置请求处理器
        httpServer.requestHandler(new HttpServerHandler());
        // 启动服务器并监听指定端口
        httpServer.listen(port, res -> {
            if (res.succeeded()) {
                log.info("HttpServer started on port {}", port);
            } else {
                log.info("Failed to start HttpServer: {}", res.cause().getMessage());
            }
        });
    }
}