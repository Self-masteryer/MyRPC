package com.lcx.rpc.serializer;

import java.io.IOException;

/**
 * 序列化器
 */
public interface Serializer {

    /**
     * 序列化
     * @param object 对象
     * @return 二进制数据
     * @param <T> 泛型参数
     * @throws IOException io异常
     */
    <T> byte[] serialize(T object) throws IOException;

    /**
     * 反序列化
     * @param data 二进制数据
     * @param clazz 字节码对象
     * @return 对象
     * @param <T> 泛型参数
     * @throws IOException io异常
     */
    <T> T deserialize(byte[] data, Class<T> clazz) throws IOException;

}
