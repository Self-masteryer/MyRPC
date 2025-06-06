package com.lcx.rpc.cluster.register;

import com.lcx.rpc.bootstrap.config.MyRpcApplication;
import com.lcx.rpc.common.spi.SpiLoader;

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
    public static final Registry registry = getRegistry(MyRpcApplication.getRpcConfig().getCluster().getRegistry().getType());

    /**
     * 获取注册中心实例
     */
    public static Registry getRegistry(String key) {
        return SpiLoader.getInstance(Registry.class, key);
    }
}
