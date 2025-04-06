package com.lcx.rpc.bootstrap;

import com.lcx.rpc.bootstrap.config.MyRpcApplication;

/**
 * 服务消费者启动类
 */
public class ConsumerBootStrap {
    public static void init(){
        // Rpc框架初始化
        MyRpcApplication.init();
    }
}