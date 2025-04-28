package com.lcx.consumer;

import com.lcx.common.domain.User;
import com.lcx.common.service.IUserService;
import com.lcx.rpc.bootstrap.ConsumerBootStrap;
import com.lcx.rpc.bootstrap.proxy.ServiceProxyFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ConsumerApplication {

    public static void main(String[] args) {
        ConsumerBootStrap.init();
        IUserService userService = ServiceProxyFactory.getProxy(IUserService.class, 10000);
        userService.getUserById(0);
    }

    public static void test() {
        int success = 0, failure = 0;
        ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
        // 动态代理
        IUserService userService = ServiceProxyFactory.getProxy(IUserService.class, 10000);
        for (int i = 0; i < 100; i++) {
            // 调用服务
            try {
                User user = userService.getUserById(threadLocalRandom.nextInt());
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