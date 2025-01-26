package com.lcx.rpc.register;

import com.lcx.rpc.config.RpcApplication;
import com.lcx.rpc.spi.SpiLoader;

/**
 * 注册中心工厂
 */
public class RegistryFactory {

    static {
        SpiLoader.load(Registry.class);
    }

    /**
     * 配置的注册中心
     */
    public static final Registry registry = getRegistry(RpcApplication.getRpcConfig().getRegistry().getType());

    /**
     * 获取注册中心实例
     */
    public static Registry getRegistry(String key) {
        return SpiLoader.getInstance(Registry.class, key);
    }
}
