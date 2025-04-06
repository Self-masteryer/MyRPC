package com.lcx.rpc.protocol;

import com.lcx.rpc.common.constant.ProtocolConstant;
import com.lcx.rpc.bootstrap.config.MyRpcApplication;
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
         * 消息类型(请求/响应)
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
        return Header.builder()
                .magicNum(ProtocolConstant.PROTOCOL_MAGIC)
                .version(ProtocolConstant.PROTOCOL_VERSION);
    }

    public static Header.HeaderBuilder getDefReqHeader() {
        ProtocolMessageSerializerEnum serializerEnum = ProtocolMessageSerializerEnum.getByValue(
                MyRpcApplication.getRpcConfig().getProtocol().getSerializer());
        return getBasedHeader()
                .serializerId((byte) serializerEnum.getKey())
                .messageType((byte) ProtocolMessageTypeEnum.REQUEST.getValue());
    }
}
