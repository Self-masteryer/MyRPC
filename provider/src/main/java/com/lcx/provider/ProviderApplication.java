package com.lcx.provider;

import com.lcx.rpc_core.config.RpcApplication;
import com.lcx.rpc_core.config.RpcConfig;
import com.lcx.common.service.IUserService;
import com.lcx.rpc_core.register.LocalRegister;
import com.lcx.rpc_core.server.ipml.VertxHttpServerImpl;
import com.lcx.provider.service.ipml.UserServiceImpl;

public class ProviderApplication {

    public static void main(String[] args) {
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        // 注册服务：接口全限定名、实现类字节码类
        LocalRegister.register(IUserService.class.getName(), UserServiceImpl.class);
        // 提供服务
        VertxHttpServerImpl vertxHttpServer = new VertxHttpServerImpl();
        vertxHttpServer.doStart(rpcConfig.getPort());
    }
}
