package com.lcx.rpc.transport.server.tcp.vertx.codec;

import com.lcx.rpc.common.model.RpcRequest;
import com.lcx.rpc.common.model.RpcResponse;
import com.lcx.rpc.common.constant.ProtocolConstant;
import com.lcx.rpc.protocol.ProtocolMessage;
import com.lcx.rpc.protocol.enums.ProtocolMessageSerializerEnum;
import com.lcx.rpc.protocol.enums.ProtocolMessageTypeEnum;
import com.lcx.rpc.protocol.serializer.Serializer;
import com.lcx.rpc.protocol.serializer.SerializerFactory;
import io.vertx.core.buffer.Buffer;

import java.io.IOException;

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
                .version(buffer.getByte(1))
                .serializerId(buffer.getByte(2))
                .messageType(buffer.getByte(3))
                .requestId(buffer.getLong(5))
                .bodyLength(buffer.getInt(13))
                .build();

        // 反序列化
        byte[] bodyBytes = buffer.getBytes(17, 17 + header.getBodyLength());
        // 获取序列化器
        ProtocolMessageSerializerEnum serializerEnum = ProtocolMessageSerializerEnum.getByKey(header.getSerializerId());
        if(serializerEnum == null) {
            throw new RuntimeException("序列化消息的协议不存在");
        }
        Serializer serializer = SerializerFactory.getSerializer(serializerEnum.getValue());
        // 获取消息类型
        ProtocolMessageTypeEnum messageTypeEnum = ProtocolMessageTypeEnum.getByValue(header.getMessageType());
        if (messageTypeEnum == null) {
            throw new RuntimeException("序列化消息的类型不存在");
        }

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
