package com.lcx.examplespringbootprovider.service.impl;

import cn.hutool.core.lang.UUID;
import com.lcx.common.model.User;
import com.lcx.common.service.IUserService;
import com.lcx.rpc.springboot.starter.annotation.RpcService;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * 用户服务实现类
 */
@Service
@RpcService
public class UserServiceImpl implements IUserService {

    @Override
    public User getUserById(Integer uid) {
        System.out.println("客户端查询了" + uid + "的用户");
        // 模拟从数据库中取用户的行为
        Random random = new Random();
        //random.nextBoolean()：随机生成 true 或 false，表示用户的性别。
        return User.builder()
                .userName(UUID.randomUUID().toString())
                .id(uid)
                .sex(random.nextBoolean())
                .build();
    }

    @Override
    public Integer insertUser(User user) {
        System.out.println("插入数据成功" + user.getUserName());
        return user.getId();
    }

}