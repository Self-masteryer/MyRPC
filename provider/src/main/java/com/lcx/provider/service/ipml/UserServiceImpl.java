package com.lcx.provider.service.ipml;

import com.lcx.common.model.User;
import com.lcx.common.service.IUserService;

import java.util.Objects;

/**
 * 用户服务实现类
 */
public class UserServiceImpl implements IUserService {
    @Override
    public User getUser(User user) {
        if (Objects.equals(user.getName(), "xx")) throw new RuntimeException();
        System.out.println("用户名：" + user.getName());
        return user;
    }
}
