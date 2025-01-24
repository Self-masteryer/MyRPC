package com.lcx.common.service;

import com.lcx.common.model.User;

/**
 * 用户服务接口
 */
public interface IUserService {

    /**
     * 获取用户
     * @param user
     * @return
     */
    User getUser(User user);
}
