package com.lcx.rpc.transport.server.tcp.netty.codec;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import static com.lcx.rpc.common.constant.ProtocolConstant.MESSAGE_BODY_LENGTH_OFFSET;
import static com.lcx.rpc.common.constant.ProtocolConstant.MESSAGE_MAX_LENGTH;

public class ProtocolFrameDecoder extends LengthFieldBasedFrameDecoder {

    public ProtocolFrameDecoder() {
        super(MESSAGE_MAX_LENGTH, MESSAGE_BODY_LENGTH_OFFSET, 4, 0, 0);
    }
}
