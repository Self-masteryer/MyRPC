package com.lcx.rpc.common.constant;

import lombok.Data;

@Data
public class HeartbeatConstant {

    /**
     * 读空闲时间
     */
    public static final int readerIdleTime = 60;
    /**
     * 写空闲时间
     */
    public static final int writerIdleTime = 0;
    /**
     * 读写空闲时间
     */
    public static final int allIdleTime = 0;
    /**
     * 心跳间隔
     */
    public static final int heartbeatInterval = readerIdleTime - 10;

}
