package com.lcx.rpc.server.tcp.netty.codec;

import com.lcx.rpc.model.RpcRequest;
import com.lcx.rpc.model.RpcResponse;
import com.lcx.rpc.protocol.ProtocolConstant;
import com.lcx.rpc.protocol.ProtocolMessage;
import com.lcx.rpc.protocol.enums.ProtocolMessageSerializerEnum;
import com.lcx.rpc.protocol.enums.ProtocolMessageTypeEnum;
import com.lcx.rpc.serializer.Serializer;
import com.lcx.rpc.serializer.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

/**
 * 协议信息编解码器
 */
@ChannelHandler.Sharable
public class ProtocolMessageCodec extends MessageToMessageCodec<ByteBuf, ProtocolMessage<?>> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ProtocolMessage<?> protocolMessage, List<Object> list) throws Exception {
        ProtocolMessage.Header header = protocolMessage.getHeader();
        ByteBuf buffer = ctx.alloc().buffer();
        buffer.writeInt(header.getMagicNum())
                .writeInt(header.getHeaderLength())
                .writeByte(header.getVersion())
                .writeByte(header.getMessageType())
                .writeByte(header.getSerializerId())
                .writeLong(header.getRequestId());

        ProtocolMessageSerializerEnum serializerEnum = ProtocolMessageSerializerEnum.getByKey(header.getSerializerId());
        assert serializerEnum != null : "不支持的序列化器";

        Serializer serializer = SerializerFactory.getSerializer(serializerEnum.getValue());
        byte[] bytes = serializer.serialize(protocolMessage.getBody());
        buffer.writeInt(bytes.length).writeBytes(bytes);

        list.add(buffer);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) throws Exception {
        int magicNum = byteBuf.readInt();
        // 校验魔数
        if (magicNum != ProtocolConstant.PROTOCOL_MAGIC) {
            throw new RuntimeException("消息 magic 非法");
        }
        // 解析请求头
        ProtocolMessage.Header header = ProtocolMessage.Header.builder()
                .magicNum(magicNum)
                .headerLength(byteBuf.readInt())
                .version(byteBuf.readByte())
                .messageType(byteBuf.readByte())
                .serializerId(byteBuf.readByte())
                .requestId(byteBuf.readLong())
                .bodyLength(byteBuf.readInt())
                .build();

        byte[] body = new byte[header.getBodyLength()];
        byteBuf.readBytes(body);

        // 序列化器
        ProtocolMessageSerializerEnum serializerEnum = ProtocolMessageSerializerEnum.getByKey(header.getSerializerId());
        assert serializerEnum != null : "序列化消息的协议不存在";
        Serializer serializer = SerializerFactory.getSerializer(serializerEnum.getValue());

        // 消息类型
        ProtocolMessageTypeEnum messageTypeEnum = ProtocolMessageTypeEnum.getByValue(header.getMessageType());
        assert messageTypeEnum != null : "序列化消息的类型不存在";

        switch (messageTypeEnum) {
            case REQUEST -> {
                RpcRequest request = serializer.deserialize(body, RpcRequest.class);
                list.add(new ProtocolMessage<>(header, request));
            }
            case RESPONSE -> {
                RpcResponse response = serializer.deserialize(body, RpcResponse.class);
                list.add(new ProtocolMessage<>(header, response));
            }
            default -> throw new RuntimeException("暂不支持该消息类型");
        }
        ;

    }
}
