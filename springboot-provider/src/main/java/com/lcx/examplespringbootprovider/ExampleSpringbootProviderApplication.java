package com.lcx.examplespringbootprovider;

import com.lcx.rpc.springboot.starter.annotation.EnableMyRpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableMyRpc
@SpringBootApplication
public class ExampleSpringbootProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleSpringbootProviderApplication.class, args);
    }

}
