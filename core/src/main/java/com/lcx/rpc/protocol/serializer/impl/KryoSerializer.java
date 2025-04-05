package com.lcx.rpc.protocol.serializer.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import com.lcx.rpc.common.model.RpcRequest;
import com.lcx.rpc.common.model.RpcResponse;
import com.lcx.rpc.protocol.serializer.Serializer;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.IOException;

public class KryoSerializer implements Serializer {

    /**
     * 初始缓冲区大小（根据业务调整，建议设置为典型对象大小的2倍）
     */
    private static final int DEFAULT_BUFFER_SIZE = 4096;

    /**
     * Kryo线程不安全，使用ThreadLocal保证每个线程独立实例
     */
    private static final ThreadLocal<Kryo> KRYO_THREAD_LOCAL = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false); // 允许动态注册
        kryo.setReferences(true);            // 支持循环引用
        kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy())); // 解决无默认构造器类的问题
        // 手动注册核心业务类
        kryo.register(RpcRequest.class);
        kryo.register(RpcResponse.class);
        kryo.register(Object[].class); // 显式注册 Object 数组类型
        return kryo;
    });

    // 复用Input对象（每个线程独立）
    private static final ThreadLocal<Input> INPUT_THREAD_LOCAL = ThreadLocal.withInitial(() ->
            new Input(DEFAULT_BUFFER_SIZE));

    // 复用Output对象（每个线程独立）
    private static final ThreadLocal<Output> OUTPUT_THREAD_LOCAL = ThreadLocal.withInitial(() ->
            new Output(DEFAULT_BUFFER_SIZE, -1));

    @Override
    public <T> byte[] serialize(T object) throws IOException {
        try {
            Output output = OUTPUT_THREAD_LOCAL.get();
            output.reset(); // 清空旧数据
            KRYO_THREAD_LOCAL.get().writeObject(output, object);
            return output.toBytes();
        } catch (Exception e) {
            throw new IOException("Serialization failed: " + e.getMessage(), e); // 保留原始异常信息
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws IOException {
        try {
            Input input = INPUT_THREAD_LOCAL.get();
            input.setBuffer(data);
            return KRYO_THREAD_LOCAL.get().readObject(input, clazz);
        } catch (Exception e) {
            throw new IOException("Deserialization failed: " + e.getMessage(), e); // 保留原始异常信息
        }
    }

}
