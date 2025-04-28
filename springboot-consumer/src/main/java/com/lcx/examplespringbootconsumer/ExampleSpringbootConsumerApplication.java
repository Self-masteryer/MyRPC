package com.lcx.examplespringbootconsumer;

import com.lcx.rpc.springboot.starter.annotation.EnableMyRpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableMyRpc(needServer = false)
@SpringBootApplication
public class ExampleSpringbootConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleSpringbootConsumerApplication.class, args);
    }

}
