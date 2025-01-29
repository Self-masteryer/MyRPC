package com.lcx.rpc.server.tcp;

import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import org.junit.Test;

public class VertxTcpServerTest {

    @Test
    public void test() {
        Vertx vertx = Vertx.vertx();
        NetServer server = vertx.createNetServer();
        // 连接处理
        server.connectHandler(socket -> {
            socket.handler(buffer -> {
                String testMessage = "Hello, server!Hello, server!Hello, server!Hello, server!";
                int messageLength = testMessage.getBytes().length;
                if (buffer.getBytes().length < messageLength) {
                    System.out.println("半包, length = " + buffer.getBytes().length);
                    return;
                }
                if (buffer.getBytes().length > messageLength) {
                    System.out.println("粘包, length = " + buffer.getBytes().length);
                    return;
                }
                String str = new String(buffer.getBytes(0, messageLength));
                System.out.println(str);
                if (testMessage.equals(str)) {
                    System.out.println("good");
                }
            });
        });
        // 启动 TCP 服务器并监听指定端口
        server.listen(8080,result->{
            if (result.succeeded()) {
                System.out.println("TCP server started on port " + 8080);
            } else {
                System.out.println("Failed to start TCP server: " + result.cause());
            }
        });
    }
}