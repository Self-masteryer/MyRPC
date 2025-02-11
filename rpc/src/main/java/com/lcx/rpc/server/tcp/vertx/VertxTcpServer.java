package com.lcx.rpc.server.tcp.vertx;

import com.lcx.rpc.server.RpcServer;
import com.lcx.rpc.server.handler.RpcReqHandler;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;

public class VertxTcpServer implements RpcServer {

    @Override
    public void doStart(int port, RpcReqHandler reqHandler) {
        Vertx vertx = Vertx.vertx();
        NetServer server = vertx.createNetServer();
        server.connectHandler(new VertxTcpServerHandler(reqHandler));
        server.listen(port, result -> {
            if (result.succeeded()) {
                System.out.println("TCP服务器启动成功，监听端口：" + port);
            } else {
                System.out.println("TCP服务器启动失败：" + result.cause());
            }
        });
    }

}
