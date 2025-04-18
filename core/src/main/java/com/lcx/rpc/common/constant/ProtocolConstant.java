package com.lcx.rpc.common.constant;

public final class ProtocolConstant {

    /**
     * 协议魔数
     */
    public static final short PROTOCOL_MAGIC = (short) 0xD0BC;

    /**
     * 协议版本号
     */
    public static final byte PROTOCOL_VERSION = 0x1;

    /**
     * 消协议体长度偏移量
     */
    public static final int MESSAGE_BODY_LENGTH_OFFSET = 14;

    /**
     * 协议头固定长度
     */
    public static final int PROTOCOL_HEADER_LENGTH = 18;

    /**
     * 消息最大长度
     */
    public static final int MESSAGE_MAX_LENGTH = 1024 * 8;
}
