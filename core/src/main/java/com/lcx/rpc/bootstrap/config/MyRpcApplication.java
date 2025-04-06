package com.lcx.rpc.bootstrap.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lcx.rpc.cluster.register.Registry;
import com.lcx.rpc.cluster.register.RegistryFactory;
import com.lcx.rpc.common.utils.ConfigUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * MyRPC配置文件
 */
@Data
@Slf4j
public class MyRpcApplication {

    private MyRpcConfig myRPC;
    private static volatile MyRpcApplication myRpcApplication;

    // 懒汉式单例模式：双重检查锁，搭配volatile关键之保证线程安全
    public static MyRpcConfig getRpcConfig() {
        if (myRpcApplication == null) {
            synchronized (MyRpcApplication.class) {
                if (myRpcApplication == null) {
                    build();
                }
            }
        }
        return myRpcApplication.getMyRPC();
    }

    private static void build() {
        try {
            // 加载配置文件
            myRpcApplication = ConfigUtils.loadConfig(MyRpcApplication.class, "");
        } catch (Exception e) {
            // 加载失败，采用默认值
            myRpcApplication = new MyRpcApplication();
        }
        log.info("MyRPC config:{}", myRpcApplication);
    }

    /**
     * 初始化框架，支持自定义配置
     */
    public static void init() {
        // 注册中心配置
        RegistryConfig registryConfig = getRpcConfig().getCluster().getRegistry();
        // 注册中心
        Registry registry = RegistryFactory.registry;
        log.info("registry init success, config = {}", registryConfig);
        // 创建并注册Shutdown Hook，jvm退出时销毁注册中心
        Runtime.getRuntime().addShutdownHook(new Thread(registry::destroy));
    }
}
