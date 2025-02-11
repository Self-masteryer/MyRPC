package com.lcx.rpc.server.tcp.vertx;

import com.lcx.rpc.server.RpcServer;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;

public class VertxTcpServer implements RpcServer<NetSocket> {

    @Override
    public void doStart(int port, Handler<NetSocket> handler) {
        Vertx vertx = Vertx.vertx();
        NetServer server = vertx.createNetServer();
        server.connectHandler(handler);
        server.listen(port, result -> {
            if (result.succeeded()) {
                System.out.println("TCP服务器启动成功，监听端口：" + port);
            } else {
                System.out.println("TCP服务器启动失败：" + result.cause());
            }
        });
    }

}
