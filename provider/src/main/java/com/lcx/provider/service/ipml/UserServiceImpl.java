package com.lcx.provider.service.ipml;

import cn.hutool.core.lang.UUID;
import com.lcx.common.domain.User;
import com.lcx.common.service.IUserService;
import com.lcx.rpc.cluster.fault.retry.Retryable;
import com.lcx.rpc.common.exception.BusinessException;
import com.lcx.rpc.common.exception.RetryableException;

import java.util.Random;

/**
 * 用户服务实现类
 */
public class UserServiceImpl implements IUserService {

    @Override
    @Retryable
    public User getUserById(Integer uid) {
        System.out.println("客户端查询了" + uid + "的用户");
        if (uid == 0) {
            throw new RetryableException(new RuntimeException("可重试异常"));
        } else if (uid == 1) {
            throw new BusinessException(new RuntimeException("业务异常"));
        }
        // 模拟从数据库中取用户的行为
        Random random = new Random();
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
