package com.lcx.examplespringbootprovider;

import com.lcx.common.model.User;
import com.lcx.examplespringbootprovider.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ExampleSpringbootProviderApplicationTests {

    @Autowired
    private UserServiceImpl userService;

    @Test
    void contextLoads() {
        userService.getUserById(1);
    }

}
