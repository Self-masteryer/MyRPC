package com.lcx.rpc.bootstrap;

import com.lcx.rpc.config.RpcApplication;

/**
 * 服务消费者启动类
 */
public class ConsumerBootStrap {
    public static void init(){
        // Rpc框架初始化
        RpcApplication.init();
    }
}