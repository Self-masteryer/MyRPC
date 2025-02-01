package com.lcx.consumer;

import com.lcx.common.model.User;
import com.lcx.common.service.IUserService;
import com.lcx.rpc.bootstrap.ConsumerBootStrap;
import com.lcx.rpc.proxy.ServiceProxyFactory;

public class ConsumerApplication {
    public static void main(String[] args) {
        ConsumerBootStrap.init();
        // 动态代理
        IUserService userService = ServiceProxyFactory.getProxy(IUserService.class);
        User user = new User();
        user.setName("lcx");
        // 调用服务
        User newUser = userService.getUser(user);
        if (newUser != null) {
            System.out.println("用户名：" + newUser.getName());
        } else {
            System.out.println("用户不存在");
        }
    }
}