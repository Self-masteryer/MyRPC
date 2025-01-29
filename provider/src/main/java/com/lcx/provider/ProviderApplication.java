package com.lcx.provider;

import com.lcx.provider.service.ipml.UserServiceImpl;
import com.lcx.rpc.config.RpcApplication;
import com.lcx.rpc.config.RpcConfig;
import com.lcx.rpc.register.LocalRegister;
import com.lcx.rpc.server.tcp.VertxTcpServer;

public class ProviderApplication {

    public static void main(String[] args) {
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        LocalRegister.register(rpcConfig.getName(), UserServiceImpl.class);
        // 提供服务
        VertxTcpServer vertxTcpServer = new VertxTcpServer();
        vertxTcpServer.doStart(rpcConfig.getPort());
    }
}
