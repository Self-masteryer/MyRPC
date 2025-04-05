package com.lcx.rpc.common.constant;

public final class ProtocolConstant {

    /**
     * 协议魔数
     */
    public static final int PROTOCOL_MAGIC = 0x00000001;

    /**
     * 协议版本号
     */
    public static final byte PROTOCOL_VERSION = 0x1;

    /**
     * 消息体长度偏移量
     */
    public static final int MESSAGE_BODY_LENGTH_OFFSET =19;

    /**
     * 协议首都长度
     */
    public static final int PROTOCOL_HEADER_LENGTH = 23;

    /**
     * 消息最大长度
     */
    public static final int MESSAGE_MAX_LENGTH = 1024*8;
}
