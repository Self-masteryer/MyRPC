package com.lcx.rpc.serializer.impl;

import com.lcx.rpc.serializer.Serializer;

import java.io.*;

/**
 * jdk自带的序列化器
 */
public class JdkSerializer implements Serializer {

    @Override
    public <T> byte[] serialize(T object) throws IOException {
        if (object == null) {
            throw new IllegalArgumentException("Object must not be null");
        }
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(object);
            return byteArrayOutputStream.toByteArray();
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> type) throws IOException {
        if (bytes == null) {
            throw new IllegalArgumentException("Byte array must not be null");
        }
        try (ByteArrayInputStream is = new ByteArrayInputStream(bytes);
             ObjectInputStream objectInputStream = new ObjectInputStream(is)) {
            Object obj = objectInputStream.readObject();
            if (type.isInstance(obj)) {
                return type.cast(obj);
            } else {
                throw new InvalidObjectException("Deserialized object is not of type " + type.getName());
            }
        } catch (ClassNotFoundException e) {
            throw new IOException("Class not found during deserialization", e);
        }
    }

}