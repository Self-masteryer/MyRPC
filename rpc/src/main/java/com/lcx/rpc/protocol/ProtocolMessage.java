package com.lcx.rpc.protocol;

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
        private byte magic;
        /**
         * 版本号
         */
        private byte version;
        /**
         * 序列化器
         */
        private byte serializer;
        /**
         * 消息类型(请求/响应)
         */
        private byte type;
        /**
         *  状态
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
}
