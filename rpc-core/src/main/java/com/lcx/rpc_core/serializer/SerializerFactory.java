package com.lcx.rpc_core.serializer;

import java.util.HashMap;

/**
 * 序列化器工厂：享元设计模式
 */
public class SerializerFactory {

    /**
     * 序列化映射
     */
    private static final HashMap<String, Serializer> SERIALIZER_MAP =  new HashMap<String, Serializer>(){{
        put(SerializerKeys.HESSIAN, new HessianSerializer());
        put(SerializerKeys.JSON, new JsonSerializer());
        put(SerializerKeys.KRYO, new KryoSerializer());
        put(SerializerKeys.JDK, new JdkSerializer());
    }};

    /**
     * 默认序列化器
     */
    private static final Serializer DEFAULT_SERIALIZER = SERIALIZER_MAP.get(SerializerKeys.JDK);

    /**
     * 获取实例
     * @param key
     * @return
     */
    public static Serializer getSerializer(String key){
        return SERIALIZER_MAP.getOrDefault(key, DEFAULT_SERIALIZER);
    }

}