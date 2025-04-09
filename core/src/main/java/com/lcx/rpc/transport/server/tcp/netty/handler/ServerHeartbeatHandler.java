package com.lcx.rpc.transport.server.tcp.netty.handler;

import com.lcx.rpc.common.constant.HeartbeatConstant;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 服务器心跳处理器：关闭连接
 */
@Slf4j
public class ServerHeartbeatHandler extends ChannelDuplexHandler {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        try {
            // 处理 IdleStateEvent 空闲事件
            if (evt instanceof IdleStateEvent idleStateEvent) {
                IdleState idleState = idleStateEvent.state();
                if (idleState == IdleState.READER_IDLE) { // 读空闲
                    log.info("超过{}秒没有接受数据,channel:{}", HeartbeatConstant.readerIdleTime, ctx.channel());
                } else if (idleState == IdleState.WRITER_IDLE) { // 写空闲
                    log.info("超过{}s没有发送数据,channel:{}", HeartbeatConstant.writerIdleTime, ctx.channel());
                } else if (idleState == IdleState.ALL_IDLE) { // 读写空闲
                    log.info("超过{}s没有读写数据,channel:{}", HeartbeatConstant.allIdleTime, ctx.channel());
                }
                ctx.close(); // 关闭连接
            }
        } catch (Exception e) {
            log.error("处理事件发生异常" + e);
        }
    }
}