package com.lcx.rpc.transport.client.netty;

import cn.hutool.core.util.IdUtil;
import com.lcx.rpc.bootstrap.config.ClientConfig;
import com.lcx.rpc.bootstrap.config.MyRpcApplication;
import com.lcx.rpc.common.constant.HeartbeatConstant;
import com.lcx.rpc.common.model.RpcRequest;
import com.lcx.rpc.common.model.RpcResponse;
import com.lcx.rpc.common.model.ServiceMetaInfo;
import com.lcx.rpc.protocol.ProtocolMessage;
import com.lcx.rpc.transport.server.tcp.netty.codec.ProtocolFrameDecoder;
import com.lcx.rpc.transport.server.tcp.netty.codec.ProtocolMessageCodec;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
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
    private static final NioEventLoopGroup SHARED_GROUP;
    // TCP 连接池
    private static final ChannelPoolMap<InetSocketAddress, FixedChannelPool> POOL_MAP;
    // 请求映射表
    private static final Map<Long, CompletableFuture<RpcResponse>> PENDING_REQUESTS = new ConcurrentHashMap<>();

    static {
        ClientConfig clientConfig = MyRpcApplication.getRpcConfig().getClient();
        SHARED_GROUP = new NioEventLoopGroup(clientConfig.getIoThreads());
        POOL_MAP = new AbstractChannelPoolMap<>() {
            @Override
            protected FixedChannelPool newPool(InetSocketAddress key) {
                Bootstrap bootstrap = new Bootstrap().group(SHARED_GROUP)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, clientConfig.getConnectTimeout())
                        .remoteAddress(key);
                return new FixedChannelPool(bootstrap, new ChannelInitialHandler(), 50);
            }
        };
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

        ChannelPool channelPool = POOL_MAP.get(new InetSocketAddress(metaInfo.getHost(), metaInfo.getPort()));
        channelPool.acquire() // 异步获取连接
                .addListener(future -> { // 添加回调函数
                    if (future.isSuccess()) { // 成功获取连接
                        Channel channel = (Channel) future.getNow();
                        ProtocolMessage<RpcRequest> message = ProtocolMessage.createReq(requestId, request);
                        channel.writeAndFlush(message);
                        channelPool.release(channel);  // 显式释放连接，否则导致连接泄漏
                    } else { // 获取连接失败
                        resultFuture.completeExceptionally(future.cause());
                    }
                });

        return resultFuture;
    }

    public static void shutdown() {
        SHARED_GROUP.shutdownGracefully();
        // 关闭所有连接池

    }

    /**
     * RPC响应处理器
     */
    @ChannelHandler.Sharable
    private static class RpcResponseHandler extends SimpleChannelInboundHandler<ProtocolMessage<RpcResponse>> {
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

    /**
     * 动态地址channel初始化处理器
     */
    private static class ChannelInitialHandler implements ChannelPoolHandler {

        /**
         * 初始化Channel的配置，添加处理器
         */
        @Override // 当连接池中创建新 Channel 时调用，仅调用一次
        public void channelCreated(Channel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            try {
                pipeline.addLast(new ProtocolFrameDecoder()) // 帧解析器
                        .addLast(new ProtocolMessageCodec()) // 协议编解码器
                        .addLast(new RpcResponseHandler())  // RPC响应处理器
                        .addLast(new IdleStateHandler(0, HeartbeatConstant.heartbeatInterval, 0, TimeUnit.SECONDS))
                        .addLast(new ClientHeartbeatHandler());
            } catch (Exception e) {
                log.error("Error initializing Netty client pipeline", e);
                throw e;  // 重新抛出异常，确保管道初始化失败时处理正确
            }
        }

        @Override // 从连接池中成功获取 Channel 时调用
        public void channelAcquired(Channel ch) throws Exception {
        }

        @Override // Channel 被放回连接池时调用
        public void channelReleased(Channel ch) throws Exception {
            if (!ch.isActive()) { // 客户端主动关闭
                log.info("Channel:{} 已关闭", ch);
                ch.close();
            }
        }
    }
}
