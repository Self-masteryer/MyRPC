package com.lcx.consumer;

import com.lcx.common.domain.User;
import com.lcx.common.service.IUserService;
import com.lcx.rpc.bootstrap.ConsumerBootStrap;
import com.lcx.rpc.bootstrap.proxy.ServiceProxyFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class ConsumerApplication {

    public static void main(String[] args) {
        test();
    }

    public static void test() {
        ConsumerBootStrap.init();
        int success = 0, failure = 0;
        // 动态代理
        IUserService userService = ServiceProxyFactory.getProxy(IUserService.class);
        for (int i = 0; i < 1; i++) {
            // 调用服务
            try {
                User user = userService.getUserById(1);
                System.out.println("用户名：" + user.getUserName());
                TimeUnit.MILLISECONDS.sleep(80);
                success++;
            } catch (Exception e) {
                System.out.println("用户不存在");
                failure++;
            }
        }
        System.out.println("success: " + success);
        System.out.println("failure: " + failure);
    }
}