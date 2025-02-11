package com.lcx.rpc.protocol;

import com.lcx.rpc.config.RpcApplication;
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
         * 版本号
         */
        private byte version;
        /**
         * 序列化器编号
         */
        private byte serializerNum;
        /**
         * 消息类型(请求/响应)
         */
        private byte messageType;
        /**
         * 状态
         */
        private byte status;
        /**
         * 消息id
         */
        private long requestId;
        /**
         * 消息长度
         */
        private int length;
    }

    public ProtocolMessage(Header header) {
        this.header = header;
    }

    public static Header.HeaderBuilder getBasedHeader(){
        return Header.builder()
                .magicNum(ProtocolConstant.PROTOCOL_MAGIC)
                .version(ProtocolConstant.PROTOCOL_VERSION);
    }

    public static Header.HeaderBuilder getDefReqHeader(){
        ProtocolMessageSerializerEnum serializerEnum = ProtocolMessageSerializerEnum.getByValue(
                        RpcApplication.getRpcConfig().getSerializer());
        return getBasedHeader()
                .serializerNum((byte) serializerEnum.getKey())
                .messageType((byte) ProtocolMessageTypeEnum.REQUEST.getValue());
    }
}
