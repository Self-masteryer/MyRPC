package com.lcx.provider;

import com.lcx.provider.service.ipml.UserServiceImpl;
import com.lcx.rpc.config.RpcApplication;
import com.lcx.rpc.config.RpcConfig;
import com.lcx.rpc.register.LocalRegister;
import com.lcx.rpc.server.ipml.VertxHttpServerImpl;

public class ProviderApplication {

    public static void main(String[] args) {
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        LocalRegister.register(rpcConfig.getName(), UserServiceImpl.class);
        // 提供服务
        VertxHttpServerImpl vertxHttpServer = new VertxHttpServerImpl();
        vertxHttpServer.doStart(rpcConfig.getPort());
    }
}
