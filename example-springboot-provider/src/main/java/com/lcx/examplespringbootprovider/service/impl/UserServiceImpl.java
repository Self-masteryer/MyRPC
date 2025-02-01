package com.lcx.examplespringbootprovider.service.impl;

import com.lcx.common.model.User;
import com.lcx.common.service.IUserService;
import com.lcx.rpc.springboot.starter.annotation.RpcService;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 用户服务实现类
 */
@Service
@RpcService
public class UserServiceImpl implements IUserService {
    @Override
    public User getUser(User user) {
        if (Objects.equals(user.getName(), "xx")) throw new RuntimeException();
        System.out.println("用户名：" + user.getName());
        return user;
    }
}