package com.lcx.rpc.server.tcp.netty;

import cn.hutool.core.util.IdUtil;
import com.lcx.rpc.model.RpcRequest;
import com.lcx.rpc.model.RpcResponse;
import com.lcx.rpc.model.ServiceMetaInfo;
import com.lcx.rpc.protocol.ProtocolMessage;
import com.lcx.rpc.server.tcp.netty.codec.ProtocolFrameDecoder;
import com.lcx.rpc.server.tcp.netty.codec.ProtocolMessageCodec;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 基于netty实现的Tcp客户端
 */
@Slf4j
public class NettyClient {

    // 共享 EventLoopGroup
    private static final NioEventLoopGroup SHARED_GROUP = new NioEventLoopGroup();
    // 共享 bootstrap
    private static final Bootstrap BOOTSTRAP = new Bootstrap();
    // tcp通道缓存
    private static final Map<ServiceMetaInfo, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>();
    // 请求映射表
    private static final Map<Long, CompletableFuture<RpcResponse>> PENDING_REQUESTS = new ConcurrentHashMap<>();

    static {
        initializeBootstrap();
    }

    private static void initializeBootstrap() {
        BOOTSTRAP.group(SHARED_GROUP)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000) // 连接超时
                .option(ChannelOption.SO_KEEPALIVE, true) // 开启长连接
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        try {
                            pipeline.addLast(new ProtocolFrameDecoder())
                                    .addLast(new ProtocolMessageCodec())
                                    .addLast(new NettyClient.ClientHandler());
                        } catch (Exception e) {
                            log.error("Error initializing Netty client pipeline", e);
                            throw e;  // 重新抛出异常，确保管道初始化失败时处理正确
                        }
                    }
                });
    }

    /**
     * 发送Rpc请求
     *
     * @param request  Rpc请求
     * @param metaInfo 服务元数据
     * @return Rpc响应
     */
    public static CompletableFuture<RpcResponse> doRequest(RpcRequest request, ServiceMetaInfo metaInfo) {
        long requestId = IdUtil.getSnowflakeNextId();
        CompletableFuture<RpcResponse> resultFuture = new CompletableFuture<>();
        PENDING_REQUESTS.put(requestId, resultFuture);

        // 设置超时处理
        SHARED_GROUP.schedule(() -> {
            if (resultFuture.completeExceptionally(new TimeoutException("Request timeout"))) {
                PENDING_REQUESTS.remove(requestId);
            }
        }, 20, TimeUnit.SECONDS);

        getOrCreateChannel(metaInfo).addListener(future -> {
            if (future.isSuccess()) {
                Channel channel = ((ChannelFuture) future).channel();
                ProtocolMessage<RpcRequest> message = createProtocolMessage(request, requestId);
                channel.writeAndFlush(message);
            } else {
                resultFuture.completeExceptionally(future.cause());
            }
        });

        return resultFuture;
    }

    private static ProtocolMessage<RpcRequest> createProtocolMessage(RpcRequest request, long requestId) {
        return new ProtocolMessage<>(
                ProtocolMessage.getDefReqHeader()
                        .requestId(requestId)
                        .build()
                , request);
    }

    private static ChannelFuture getOrCreateChannel(ServiceMetaInfo metaInfo) {
        synchronized (CHANNEL_CACHE) {
            Channel existing = CHANNEL_CACHE.get(metaInfo);
            if (existing != null && existing.isActive()) {
                // 返回一个已成功的 ChannelFuture
                return existing.newSucceededFuture();
            }

            ChannelFuture future = BOOTSTRAP.connect(metaInfo.getHost(), metaInfo.getPort());
            future.addListener((ChannelFutureListener) f -> {
                if (f.isSuccess()) {
                    CHANNEL_CACHE.put(metaInfo, f.channel());
                    f.channel().closeFuture().addListener(closeFuture ->
                            CHANNEL_CACHE.remove(metaInfo));
                }
            });
            return future;
        }
    }

    public static void shutdown() {
        SHARED_GROUP.shutdownGracefully();
        CHANNEL_CACHE.values().forEach(Channel::close);
        CHANNEL_CACHE.clear();
    }

    @ChannelHandler.Sharable
    private static class ClientHandler extends SimpleChannelInboundHandler<ProtocolMessage<RpcResponse>> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ProtocolMessage<RpcResponse> msg) {
            long requestId = msg.getHeader().getRequestId();
            CompletableFuture<RpcResponse> future = PENDING_REQUESTS.remove(requestId);
            if (future != null) {
                future.complete(msg.getBody());
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
