package com.lcx.rpc.transport.server.tcp.vertx;

import com.lcx.rpc.transport.server.RpcServer;
import com.lcx.rpc.transport.server.handler.RpcReqHandler;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class VertxTcpServer implements RpcServer {

    private final RpcReqHandler rpcReqHandler;

    @Override
    public void start(int port) {
        Vertx vertx = Vertx.vertx();
        NetServer server = vertx.createNetServer();
        server.connectHandler(new VertxTcpServerHandler(rpcReqHandler));
        server.listen(port, result -> {
            if (result.succeeded()) {
                System.out.println("TCP服务器启动成功，监听端口：" + port);
            } else {
                System.out.println("TCP服务器启动失败：" + result.cause());
            }
        });
    }

}
