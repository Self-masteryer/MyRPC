package com.lcx.rpc.protocol;

import com.lcx.rpc.protocol.enums.ProtocolMessageSerializerEnum;
import com.lcx.rpc.serializer.Serializer;
import com.lcx.rpc.serializer.SerializerFactory;
import io.vertx.core.buffer.Buffer;

import java.io.IOException;

/**
 * 消息编码器
 */
public class ProtocolMessageEncoder {

    /**
     * 译码
     * @param msg 消息
     * @return 缓冲区
     * @param <T> 数据类型
     * @throws IOException I/O异常
     */
    public static <T> Buffer encode(ProtocolMessage<T> msg) throws IOException {
        if (msg == null || msg.getHeader() == null) {
            return Buffer.buffer();
        }
        // 依次向缓冲区写入字节
        Buffer buffer = Buffer.buffer();
        ProtocolMessage.Header header = msg.getHeader();
        buffer.appendByte(header.getMagic());
        buffer.appendByte(header.getVersion());
        buffer.appendByte(header.getSerializer());
        buffer.appendByte(header.getType());
        buffer.appendByte(header.getStatus());
        buffer.appendLong(header.getRequestId());
        // 获得序列化器
        ProtocolMessageSerializerEnum serializerEnum = ProtocolMessageSerializerEnum.getByKey(header.getSerializer());
        if (serializerEnum == null) {
            throw new RuntimeException("序列化协议不存在");
        }
        // 序列化
        Serializer serializer = SerializerFactory.getSerializer(serializerEnum.getValue());
        byte[] body = serializer.serialize(msg.getBody());
        // 写入 body 长度和数据
        buffer.appendInt(body.length);
        buffer.appendBytes(body);
        return buffer;
    }

}
