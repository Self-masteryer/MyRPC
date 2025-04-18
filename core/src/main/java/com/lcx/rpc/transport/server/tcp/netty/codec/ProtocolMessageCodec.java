package com.lcx.rpc.transport.server.tcp.netty.codec;

import com.lcx.rpc.common.model.RpcPing;
import com.lcx.rpc.common.model.RpcPong;
import com.lcx.rpc.common.model.RpcRequest;
import com.lcx.rpc.common.model.RpcResponse;
import com.lcx.rpc.common.constant.ProtocolConstant;
import com.lcx.rpc.protocol.ProtocolMessage;
import com.lcx.rpc.protocol.enums.ProtocolMessageSerializerEnum;
import com.lcx.rpc.protocol.enums.ProtocolMessageTypeEnum;
import com.lcx.rpc.protocol.serializer.Serializer;
import com.lcx.rpc.protocol.serializer.SerializerFactory;
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
        byte messageType = header.getMessageType();
        byte serializerId = header.getSerializerId();
        byte state = (byte) (messageType << 4 | serializerId);
        ByteBuf buffer = ctx.alloc().buffer();
        buffer.writeShort(header.getMagicNum())
                .writeByte(header.getVersion())
                .writeShort(header.getHeaderLength())
                .writeByte(state)
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
        short magicNum = byteBuf.readShort();
        // 校验魔数
        if (magicNum != ProtocolConstant.PROTOCOL_MAGIC) {
            throw new RuntimeException("消息 magic 非法");
        }
        byte version = byteBuf.readByte();
        short headerLength = byteBuf.readShort();
        byte state = byteBuf.readByte();
        byte messageType = (byte) (state >> 4);    // 高四位
        byte serializerId = (byte) (state & 0x0f); // 低四位

        // 解析请求头
        ProtocolMessage.Header header = ProtocolMessage.Header.builder()
                .magicNum(magicNum)
                .version(version)
                .headerLength(headerLength)
                .messageType(messageType)
                .serializerId(serializerId)
                .requestId(byteBuf.readLong())
                .bodyLength(byteBuf.readInt())
                .build();

        byte[] body = new byte[header.getBodyLength()];

        if (version == ProtocolConstant.PROTOCOL_VERSION) { // 相同版本

        } else if (version < ProtocolConstant.PROTOCOL_VERSION) { // 使用对应版本的解码器解码

        } else { // 忽略新字段

        }

        byteBuf.readBytes(body, 0, body.length);

        // 反序列化器
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
            case PING -> {
                RpcPing rpcPing = serializer.deserialize(body, RpcPing.class);
                list.add(new ProtocolMessage<>(header, rpcPing));
            }
            case PONG -> {
                RpcPong rpcPong = serializer.deserialize(body, RpcPong.class);
                list.add(new ProtocolMessage<>(header, rpcPong));
            }
            default -> throw new RuntimeException("暂不支持该消息类型");
        }
    }
}
