package com.lcx.examplespringbootconsumer;

import com.lcx.examplespringbootconsumer.service.impl.ExampleServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class ExampleSpringbootConsumerApplicationTests {

    @Resource
    private ExampleServiceImpl exampleServiceImpl ;

    @Test
    void test() {
        exampleServiceImpl.test();
    }

}
