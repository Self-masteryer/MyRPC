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
        // 调用服务
        User user = userService.getUserById(1);
        if (user != null) {
            System.out.println("用户名：" + user.getUserName());
        } else {
            System.out.println("用户不存在");
        }
    }
}