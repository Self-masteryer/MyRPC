package com.lcx.rpc.protocol;

public final class ProtocolConstant {

    /**
     * 协议魔数
     */
    public static final byte PROTOCOL_MAGIC = 0x1;

    /**
     * 协议版本号
     */
    public static final byte PROTOCOL_VERSION = 0x1;

    /**
     * 消息体长度偏移量
     */
    public static final int MESSAGE_LENGTH_OFFSET =13;

    /**
     * 消息头长度
     */
    public static final int MESSAGE_HEADER_LENGTH = 17;
}
