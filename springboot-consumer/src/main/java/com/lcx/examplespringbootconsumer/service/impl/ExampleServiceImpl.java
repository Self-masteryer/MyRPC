package com.lcx.examplespringbootconsumer.service.impl;

import com.lcx.common.domain.User;
import com.lcx.common.service.IUserService;
import com.lcx.examplespringbootconsumer.service.IExampleService;
import com.lcx.rpc.springboot.starter.annotation.RpcReference;
import org.springframework.stereotype.Service;

@Service
public class ExampleServiceImpl implements IExampleService {

    @RpcReference()
    private IUserService userService;

    public void test() {
        User user = userService.getUserById(1);
        System.out.println(user.getUserName());
    }
}