package com.lcx.rpc.protocol.serializer.impl;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.lcx.rpc.protocol.serializer.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HessianSerializer implements Serializer {

    @Override
    public <T> byte[] serialize(T object) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            HessianOutput ho = new HessianOutput(baos);
            ho.writeObject(object);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new IOException("Serialization failed");
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
            HessianInput hi = new HessianInput(bais);
            return clazz.cast(hi.readObject());
        } catch (IOException e) {
            throw new IOException("Deserialization failed");
        }
    }
}
