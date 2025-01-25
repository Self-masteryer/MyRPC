package com.lcx.rpc_core.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class KryoSerializer implements Serializer {

    /**
     * Kryo线程不安全，使用ThreadLocal保证每一个线程只有一个Kryo实例
     */
    private static final ThreadLocal<Kryo> KRYO_THREAD_LOCAL = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        return kryo;
    });

    @Override
    public <T> byte[] serialize(T object) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (Output output = new Output(baos)) {
            KRYO_THREAD_LOCAL.get().writeObject(output, object);
        }
        return baos.toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws IOException {
        T object;
        try (Input input = new Input(new ByteArrayInputStream(data))) {
            object = KRYO_THREAD_LOCAL.get().readObject(input, clazz);
        }
        return clazz.cast(object);
    }
}
