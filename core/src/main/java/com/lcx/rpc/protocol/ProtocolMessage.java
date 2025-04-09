package com.lcx.rpc.protocol;

import com.lcx.rpc.common.constant.ProtocolConstant;
import com.lcx.rpc.bootstrap.config.MyRpcApplication;
import com.lcx.rpc.common.model.RpcPing;
import com.lcx.rpc.common.model.RpcPong;
import com.lcx.rpc.common.model.RpcRequest;
import com.lcx.rpc.common.model.RpcResponse;
import com.lcx.rpc.protocol.enums.ProtocolMessageSerializerEnum;
import com.lcx.rpc.protocol.enums.ProtocolMessageTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 自定义rpc协议消息格式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProtocolMessage<T> {
    /**
     * 消息头
     */
    private Header header;
    /**
     * 消息体
     */
    private T body;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Header {
        /**
         * 魔数
         */
        private int magicNum;
        /**
         * 消息头长度
         */
        private int headerLength;
        /**
         * 版本号
         */
        private byte version;
        /**
         * 消息类型(请求/响应/ping/pong)
         */
        private byte messageType;
        /**
         * 序列化器Id
         */
        private byte serializerId;
        /**
         * 消息id
         */
        private long requestId;
        /**
         * 消息体长度
         */
        private int bodyLength;

        // 协议头扩展字段
    }

    public ProtocolMessage(Header header) {
        this.header = header;
    }

    public static Header.HeaderBuilder getBasedHeader() {
        ProtocolMessageSerializerEnum serializerEnum = ProtocolMessageSerializerEnum.getByValue(
                MyRpcApplication.getRpcConfig().getProtocol().getSerializer());
        assert serializerEnum != null : "序列化配置错误";
        return Header.builder()
                .magicNum(ProtocolConstant.PROTOCOL_MAGIC)
                .version(ProtocolConstant.PROTOCOL_VERSION)
                .serializerId((byte) serializerEnum.getKey());
    }

    public static Header.HeaderBuilder getReqHeader() {
        return getBasedHeader().messageType((byte) ProtocolMessageTypeEnum.REQUEST.getValue());
    }

    public static Header.HeaderBuilder getResHeader() {
        return getBasedHeader().messageType((byte) ProtocolMessageTypeEnum.RESPONSE.getValue());
    }

    public static Header.HeaderBuilder getPingHeader() {
        return getBasedHeader().messageType((byte) ProtocolMessageTypeEnum.PING.getValue());
    }

    public static Header.HeaderBuilder getPongHeader() {
        return getBasedHeader().messageType((byte) ProtocolMessageTypeEnum.PONG.getValue());
    }

    public static ProtocolMessage<RpcRequest> createReq(long requestId, RpcRequest request) {
        return new ProtocolMessage<>(
                ProtocolMessage.getReqHeader()
                        .requestId(requestId)
                        .build()
                , request);
    }

    public static ProtocolMessage<RpcResponse> createRes(long requestId, RpcResponse response) {
        return new ProtocolMessage<>(
                ProtocolMessage.getResHeader()
                        .requestId(requestId)
                        .build()
                , response);
    }

    public static ProtocolMessage<RpcPing> createPing(long requestId, RpcPing ping) {
        return new ProtocolMessage<>(
                ProtocolMessage.getPingHeader()
                        .requestId(requestId)
                        .build()
                , ping);
    }

    public static ProtocolMessage<RpcPong> createPong(long requestId, RpcPong pong) {
        return new ProtocolMessage<>(
                ProtocolMessage.getPongHeader()
                        .requestId(requestId)
                        .build()
                , pong);
    }
}
