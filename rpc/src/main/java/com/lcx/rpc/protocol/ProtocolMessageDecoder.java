package com.lcx.rpc.protocol;

import com.lcx.rpc.model.RpcRequest;
import com.lcx.rpc.model.RpcResponse;
import com.lcx.rpc.protocol.enums.ProtocolMessageSerializerEnum;
import com.lcx.rpc.protocol.enums.ProtocolMessageTypeEnum;
import com.lcx.rpc.serializer.Serializer;
import com.lcx.rpc.serializer.SerializerFactory;
import io.vertx.core.buffer.Buffer;

import java.io.IOException;

import static com.lcx.rpc.protocol.ProtocolConstant.PROTOCOL_HEADER_LENGTH;

public class ProtocolMessageDecoder {

    /**
     * 译码
     *
     * @param buffer 缓冲区
     * @return 消息
     */
    public static ProtocolMessage<?> decode(Buffer buffer) throws IOException {
        int magic = buffer.getInt(0);
        // 校验魔数
        if (magic != ProtocolConstant.PROTOCOL_MAGIC) {
            throw new RuntimeException("消息 magic 非法");
        }
        // 解析请求头
        ProtocolMessage.Header header = ProtocolMessage.Header.builder()
                .magicNum(magic)
                .headerLength(buffer.getInt(4))
                .version(buffer.getByte(8))
                .messageType(buffer.getByte(9))
                .serializerId(buffer.getByte(10))
                .requestId(buffer.getLong(11))
                .bodyLength(buffer.getInt(19))
                .build();

        // 反序列化
        byte[] bodyBytes = buffer.getBytes(PROTOCOL_HEADER_LENGTH, PROTOCOL_HEADER_LENGTH + header.getBodyLength());
        // 获取序列化器
        ProtocolMessageSerializerEnum serializerEnum = ProtocolMessageSerializerEnum.getByKey(header.getSerializerId());
        assert serializerEnum != null : "序列化消息的协议不存在";

        Serializer serializer = SerializerFactory.getSerializer(serializerEnum.getValue());
        // 获取消息类型
        ProtocolMessageTypeEnum messageTypeEnum = ProtocolMessageTypeEnum.getByValue(header.getMessageType());
        assert messageTypeEnum != null : "序列化消息的类型不存在";

        return switch (messageTypeEnum) {
            case REQUEST -> {
                RpcRequest request = serializer.deserialize(bodyBytes, RpcRequest.class);
                yield new ProtocolMessage<>(header, request);
            }
            case RESPONSE -> {
                RpcResponse response = serializer.deserialize(bodyBytes, RpcResponse.class);
                yield new ProtocolMessage<>(header, response);
            }
            default -> throw new RuntimeException("暂不支持该消息类型");
        };

    }

}
