package com.lcx.provider;

import com.lcx.common.service.IUserService;
import com.lcx.extend.register.LocalRegister;
import com.lcx.extend.server.ipml.VertxHttpServerImpl;
import com.lcx.provider.service.ipml.UserServiceImpl;

public class ProviderApplication {

    public static void main(String[] args) {
        // 注册服务
        LocalRegister.register(IUserService.class.getName(), UserServiceImpl.class);
        // 提供服务
        VertxHttpServerImpl vertxHttpServer = new VertxHttpServerImpl();
        vertxHttpServer.doStart(8080);
    }
}
