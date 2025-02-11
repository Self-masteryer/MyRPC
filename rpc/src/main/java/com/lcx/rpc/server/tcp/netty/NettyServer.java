package com.lcx.rpc.server.tcp.netty;

import com.lcx.rpc.server.tcp.netty.codec.ProtocolFrameDecoder;
import com.lcx.rpc.server.tcp.netty.codec.ProtocolMessageCodec;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;

/**
 * 基于netty实现的服务器
 */
public class NettyServer {

    public static void start(int port) {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        DefaultEventLoopGroup executors = new DefaultEventLoopGroup();
        LoggingHandler loggingHandler = new LoggingHandler();
        ProtocolMessageCodec protocolMessageCodec = new ProtocolMessageCodec();
        ServerBootstrap serverBootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ProtocolFrameDecoder())
                                .addLast(protocolMessageCodec)
                                .addLast(loggingHandler)
                                .addLast(executors,new RpcServerHandler());
                    }
                });
        ChannelFuture future = null;
        try {
            future = serverBootstrap.bind(port).sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        future.channel().closeFuture().addListener(promise->{
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            executors.shutdownGracefully();
        });
    }

}
