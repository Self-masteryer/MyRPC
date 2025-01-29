package com.lcx.rpc.server.tcp;

import io.vertx.core.Vertx;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import org.junit.Test;

public class VertxTcpClientTest {

    @Test
    public void test() {
        Vertx vertx = Vertx.vertx();
        NetClient client = vertx.createNetClient();
        client.connect(8888, "localhost", res -> {
            if(res.succeeded()) {
                System.out.println("tcp连接成功");
                NetSocket socket = res.result();
                for(int i=0;i<1000;i++){
                    socket.write("hello server!, hello server!, hello server! ");
                }
                socket.handler(buffer -> {
                    System.out.println("收到服务器消息：" + buffer.toString());
                });
            }else{
                System.out.println("tcp连接失败");
            }
        });
    }

}