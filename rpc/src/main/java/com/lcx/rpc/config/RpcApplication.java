package com.lcx.rpc.config;

import com.lcx.rpc.register.Registry;
import com.lcx.rpc.register.RegistryFactory;
import com.lcx.rpc.utils.ConfigUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Rpc配置文件
 */
@Data
@Slf4j
public class RpcApplication {

    private RpcConfig rpc;
    private static volatile RpcApplication rpcApplication;

    public static RpcConfig getRpcConfig() {
        if (rpcApplication == null) {
            synchronized (RpcApplication.class) {
                if (rpcApplication == null) {
                    build();
                }
            }
        }
        return rpcApplication.getRpc();
    }

    private static void build() {
        try {
            // 加载配置文件
            rpcApplication = ConfigUtils.loadConfig(RpcApplication.class, "");
        } catch (Exception e) {
            // 加载失败，采用默认值
            rpcApplication = new RpcApplication();
        }
        log.info("rpc config:{}", rpcApplication);
    }

    /**
     * 初始化框架，支持自定义配置
     */
    public static void init() {
        // 注册中心配置
        RegistryConfig registryConfig = getRpcConfig().getRegistry();
        // 注册中心
        Registry registry = RegistryFactory.registry;
        // 初始化
        RegistryFactory.registry.init(registryConfig);
        log.info("registry init success, config = {}", registryConfig);
        // 创建并注册Shutdown Hook，jvm退出时销毁注册中心
        Runtime.getRuntime().addShutdownHook(new Thread(registry::destroy));
    }
}
