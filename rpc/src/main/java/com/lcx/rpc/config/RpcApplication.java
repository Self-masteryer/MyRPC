package com.lcx.rpc.config;

import com.lcx.rpc.model.ServiceMetaInfo;
import com.lcx.rpc.register.Registry;
import com.lcx.rpc.register.RegistryFactory;
import com.lcx.rpc.utils.ConfigUtils;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
public class RpcApplication {

    private RpcConfig rpc;
    private static volatile RpcApplication rpcApplication;
    private static final Logger log = LoggerFactory.getLogger(RpcApplication.class);

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
        init();
    }

    /**
     * 初始化框架，支持自定义配置
     */
    public static void init() {
        // 注册中心
        RegistryConfig registryConfig = rpcApplication.getRpc().getRegistry();
        Registry registry = RegistryFactory.registry;
        registry.init(registryConfig); // 初始化
        log.info("registry init success, config = {}", registryConfig);
        // 服务注册
        try {
            RpcConfig rpc = rpcApplication.getRpc();
            ServiceMetaInfo serviceMetaInfo = ServiceMetaInfo.builder()
                    .name(rpc.getName())
                    .version(rpc.getVersion())
                    .host(rpc.getHost())
                    .port(rpc.getPort())
                    .weight(rpc.getWeight())
                    .build();
            registry.register(serviceMetaInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // 创建并注册Shutdown Hook，jvm退出时销毁注册中心
        Runtime.getRuntime().addShutdownHook(new Thread(registry::destroy));
    }
}
