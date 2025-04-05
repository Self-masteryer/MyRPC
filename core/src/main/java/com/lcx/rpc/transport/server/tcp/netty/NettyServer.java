package com.lcx.rpc.transport.server.tcp.netty;

import com.lcx.rpc.transport.server.RpcServer;
import com.lcx.rpc.transport.server.handler.RpcReqHandler;
import com.lcx.rpc.transport.server.tcp.netty.codec.ProtocolFrameDecoder;
import com.lcx.rpc.transport.server.tcp.netty.codec.ProtocolMessageCodec;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 基于netty实现的服务器
 */
@Slf4j
public class NettyServer implements RpcServer {

    private final ServerConfig config;
    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;
    private DefaultEventLoopGroup businessGroup;
    private Channel serverChannel;
    private final RpcReqHandler rpcReqHandler;

    // 默认配置构造函数
    public NettyServer(RpcReqHandler rpcReqHandler) {
        this(new ServerConfig(),rpcReqHandler);
    }

    // 自定义配置构造函数
    public NettyServer(ServerConfig config, RpcReqHandler rpcReqHandler) {
        this.config = config;
        this.rpcReqHandler = rpcReqHandler;
        validateConfig();
    }

    // 配置校验
    private void validateConfig() {
        if (config.port < 1 || config.port > 65535) {
            throw new IllegalArgumentException("Invalid port number");
        }
    }

    @Override
    public void start(int port) {
        config.setPort(port);
        initEventLoopGroups();

        ServerBootstrap bootstrap = createServerBootstrap(rpcReqHandler);
        try {
            serverChannel = bootstrap.bind(config.port).sync().channel();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        log.info("Server started on port {}", config.port);
        addShutdownHook();

        serverChannel.closeFuture().addListener(promise -> shutdownGracefully());
    }

    private void initEventLoopGroups() {
        bossGroup = new NioEventLoopGroup(config.bossThreads);
        workerGroup = new NioEventLoopGroup(config.workerThreads);
        businessGroup = new DefaultEventLoopGroup(config.businessThreads);
    }

    private ServerBootstrap createServerBootstrap(RpcReqHandler rpcReqHandler) {
        return new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, config.soBacklog)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast(new ProtocolFrameDecoder())
                                .addLast(new ProtocolMessageCodec())
                                .addLast(businessGroup, new RpcServerHandler(rpcReqHandler));
                    }
                });
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
            log.warn("Received shutdown signal for port {}", config.port);
            shutdown();
        }));
    }

    // 配置类
    public static class ServerConfig {
        private int bossThreads = 1;
        private int workerThreads = Runtime.getRuntime().availableProcessors() * 2;
        private int businessThreads = 32;
        @Setter
        private int port = 8080;
        private int soBacklog = 1024;

        public ServerConfig bossThreads(int threads) {
            this.bossThreads = threads;
            return this;
        }

        public ServerConfig workerThreads(int threads) {
            this.workerThreads = threads;
            return this;
        }

        public ServerConfig businessThreads(int threads) {
            this.businessThreads = threads;
            return this;
        }

        public ServerConfig soBacklog(int backlog) {
            this.soBacklog = backlog;
            return this;
        }
    }
}
