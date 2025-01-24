package com.lcx.common.service.impl;

import com.lcx.common.model.User;
import com.lcx.common.service.IUserService;

/**
 * 用户服务实现类
 */
public class UserServiceImpl implements IUserService {
    @Override
    public User getUser(User user) {
        System.out.println("用户名："+user.getName());
        return user;
    }
}
