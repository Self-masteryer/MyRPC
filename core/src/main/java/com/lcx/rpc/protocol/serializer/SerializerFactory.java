package com.lcx.rpc.protocol.serializer;

import com.lcx.rpc.bootstrap.config.RpcApplication;
import com.lcx.rpc.common.spi.SpiLoader;

/**
 * 序列化器工厂：享元设计模式
 */
public class SerializerFactory {

    static {
        SpiLoader.load(Serializer.class);
    }

    /**
     * 配置的序列化器
     */
    public static final Serializer serializer = getSerializer(RpcApplication.getRpcConfig().getSerializer());

    /**
     * 获取实例
     *
     * @param key 键
     * @return 序列化器
     */
    public static Serializer getSerializer(String key) {
        return SpiLoader.getInstance(Serializer.class, key);
    }
}