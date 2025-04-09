package com.lcx.rpc.transport.server.tcp.netty;

import com.lcx.rpc.bootstrap.config.MyRpcApplication;
import com.lcx.rpc.bootstrap.config.ServerConfig;
import com.lcx.rpc.common.constant.HeartbeatConstant;
import com.lcx.rpc.transport.server.RpcServer;
import com.lcx.rpc.transport.server.handler.RpcReqHandler;
import com.lcx.rpc.transport.server.tcp.netty.codec.ProtocolFrameDecoder;
import com.lcx.rpc.transport.server.tcp.netty.codec.ProtocolMessageCodec;
import com.lcx.rpc.transport.server.tcp.netty.handler.RpcServerHandler;
import com.lcx.rpc.transport.server.tcp.netty.handler.ServerHeartbeatHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 基于netty实现的服务器
 */
@Slf4j
public class NettyServer implements RpcServer {

    private final ServerConfig config;
    private final RpcReqHandler rpcReqHandler;
    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;
    private DefaultEventLoopGroup businessGroup;
    private Channel serverChannel;

    public NettyServer(RpcReqHandler rpcReqHandler) {
        this.rpcReqHandler = rpcReqHandler;
        this.config = MyRpcApplication.getRpcConfig().getServer();
    }

    @Override
    public void start(int port) {
        initEventLoopGroups();

        ServerBootstrap bootstrap = createServerBootstrap(rpcReqHandler);
        try {
            serverChannel = bootstrap.bind(config.getPort()).sync().channel();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        log.info("Server started on port {}", port);
        addShutdownHook();

        serverChannel.closeFuture().addListener(promise -> shutdownGracefully());
    }

    private void initEventLoopGroups() {
        ServerConfig.Group group = config.getGroup();
        if (group.getBossThreads() != 0) {
            bossGroup = new NioEventLoopGroup(group.getBossThreads());
        }
        if (group.getWorkerThreads() > 0) {
            workerGroup = new NioEventLoopGroup(group.getWorkerThreads());
        } else {
            workerGroup = new NioEventLoopGroup(1);
        }
        if (group.getBusinessThreads() != 0) {
            businessGroup = new DefaultEventLoopGroup(group.getBusinessThreads());
        }
    }

    private ServerBootstrap createServerBootstrap(RpcReqHandler rpcReqHandler) {
        ServerBootstrap serverBootstrap = new ServerBootstrap()
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, config.getOption().getSoBacklog())
                .option(ChannelOption.SO_REUSEADDR, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new IdleStateHandler(
                                        HeartbeatConstant.readerIdleTime,
                                        HeartbeatConstant.writerIdleTime,
                                        HeartbeatConstant.allIdleTime,
                                        TimeUnit.SECONDS))
                                .addLast(new ServerHeartbeatHandler())
                                .addLast(new ProtocolFrameDecoder())
                                .addLast(new ProtocolMessageCodec());
                        if (businessGroup == null) {
                            pipeline.addLast(new RpcServerHandler(rpcReqHandler));
                        } else {
                            pipeline.addLast(businessGroup, new RpcServerHandler(rpcReqHandler));
                        }
                    }
                });

        if (bossGroup == null) serverBootstrap.group(workerGroup);
        else serverBootstrap.group(bossGroup, workerGroup);

        return serverBootstrap;
    }

    public void shutdown() {
        if (serverChannel != null) {
            serverChannel.close();
        }
        shutdownGracefully();
    }

    private synchronized void shutdownGracefully() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully(0, 100, TimeUnit.MILLISECONDS)
                    .addListener(f -> log.info("Boss group shutdown"));
            bossGroup = null;
        }

        if (workerGroup != null) {
            workerGroup.shutdownGracefully(0, 100, TimeUnit.MILLISECONDS)
                    .addListener(f -> log.info("Worker group shutdown"));
            workerGroup = null;
        }

        if (businessGroup != null) {
            businessGroup.shutdownGracefully(0, 100, TimeUnit.MILLISECONDS)
                    .addListener(f -> log.info("Business group shutdown"));
            businessGroup = null;
        }
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.warn("Received shutdown signal for port {}", config.getPort());
            shutdown();
        }));
    }
}
