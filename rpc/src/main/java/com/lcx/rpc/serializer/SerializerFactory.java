package com.lcx.rpc.serializer;

import com.lcx.rpc.spi.SpiLoader;

/**
 * 序列化器工厂：享元设计模式
 */
public class SerializerFactory {

    static {
        SpiLoader.load(Serializer.class);
    }

    /**
     * 默认序列化器
     */
    private static final Serializer DEFAULT_SERIALIZER = SpiLoader.getInstance(Serializer.class, SerializerKeys.JDK);

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