package com.lcx.rpc.server.tcp;

import com.lcx.rpc.server.RpcServer;
import com.lcx.rpc.server.handler.TcpServerHandler;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;

public class VertxTcpServer implements RpcServer {

    public static void main(String[] args) {
        VertxTcpServer vertxTcpServer = new VertxTcpServer();
        vertxTcpServer.doStart(8888);
    }

    @Override
    public void doStart(int port) {
        // 创建vert.x实例
        Vertx vertx = Vertx.vertx();
        // 创建TCP服务器
        NetServer server = vertx.createNetServer();
        // 处理请求
        server.connectHandler(new TcpServerHandler());
        // 启动服务器,并监听端口
        server.listen(port, result ->{
            if(result.succeeded()){
                System.out.println("TCP服务器启动成功，监听端口：" + port);
            }else{
                System.out.println("TCP服务器启动失败：" + result.cause());
            }
        });
    }
}
