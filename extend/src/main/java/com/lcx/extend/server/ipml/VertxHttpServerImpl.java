package com.lcx.extend.server.ipml;

import com.lcx.extend.server.IHttpServer;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VertxHttpServerImpl implements IHttpServer {

    private static final Logger log = LoggerFactory.getLogger(VertxHttpServerImpl.class);

    @Override
    public void doStart(int port) {
        // 创建vert.x实例
        Vertx vertx = Vertx.vertx();
        // 创建http服务器
        HttpServer httpServer = vertx.createHttpServer();

        // 监听端口并处理请求
        httpServer.requestHandler(req -> {
            // 处理HTTP请求
            log.info("Received request: {} {}", req.method(), req.uri());
            // 响应请求
            req.response()
                    .putHeader("content-type", "text/plain")
                    .end("Hello HttpServer from Vert.x!");
        });
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