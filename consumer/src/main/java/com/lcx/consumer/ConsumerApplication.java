package com.lcx.consumer;

import com.lcx.common.model.User;
import com.lcx.common.service.IUserService;

public class ConsumerApplication {
    public static void main(String[] args) {
        // 动态代理:尚未完善
        IUserService userService = null;
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