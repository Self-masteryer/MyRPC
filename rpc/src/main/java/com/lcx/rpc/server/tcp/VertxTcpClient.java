package com.lcx.rpc.server.tcp;

import io.vertx.core.Vertx;
import io.vertx.core.net.NetSocket;

/**
 * Tcp客户端
 */
public class VertxTcpClient {

    public static void main(String[] args) {
        VertxTcpClient client = new VertxTcpClient();
        client.start();
    }

    public void start() {
        // 创建vert.x实例
        Vertx vertx = Vertx.vertx();
        // 创建TCP客户端
        vertx.createNetClient().connect(8888, "localhost", result -> {
            if (result.succeeded()) {
                // 连接成功
                System.out.println("连接成功");
                NetSocket socket = result.result();
                socket.write("hello server");
                socket.handler(data -> {
                    System.out.println("收到服务器消息：" + data.toString());
                });
            } else {
                //连接失败
                System.out.println("连接失败");
            }
        });
    }
}