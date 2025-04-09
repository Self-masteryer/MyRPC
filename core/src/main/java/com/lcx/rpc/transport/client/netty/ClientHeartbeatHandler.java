package com.lcx.rpc.transport.client.netty;

import com.lcx.rpc.common.constant.HeartbeatConstant;
import com.lcx.rpc.common.model.RpcPing;
import com.lcx.rpc.protocol.ProtocolMessage;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 客户端心跳处理器：发送心跳
 */
@Slf4j
public class ClientHeartbeatHandler extends ChannelDuplexHandler {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent idleStateEvent) {
            IdleState idleState = idleStateEvent.state();
            if (idleState == IdleState.WRITER_IDLE) {
                ctx.writeAndFlush(ProtocolMessage.createPing(0L, new RpcPing()));
                log.info("超过{}秒没有写数据，发送心跳包", HeartbeatConstant.heartbeatInterval);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}