package com.lcx.rpc.server.tcp.vertx;

import cn.hutool.core.util.IdUtil;
import com.esotericsoftware.minlog.Log;
import com.lcx.rpc.config.RpcApplication;
import com.lcx.rpc.model.RpcRequest;
import com.lcx.rpc.model.RpcResponse;
import com.lcx.rpc.model.ServiceMetaInfo;
import com.lcx.rpc.protocol.ProtocolConstant;
import com.lcx.rpc.protocol.ProtocolMessage;
import com.lcx.rpc.protocol.ProtocolMessageDecoder;
import com.lcx.rpc.protocol.ProtocolMessageEncoder;
import com.lcx.rpc.protocol.enums.ProtocolMessageSerializerEnum;
import com.lcx.rpc.protocol.enums.ProtocolMessageTypeEnum;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * 基于Vertx实现的Tcp客户端
 */
@Slf4j
public class VertxTcpClient {

    /**
     * 发送Rpc请求
     *
     * @param rpcRequest      Rpc请求
     * @param serviceMetaInfo 服务元数据
     * @return Rpc响应
     */
    public static RpcResponse doRequest(RpcRequest rpcRequest, ServiceMetaInfo serviceMetaInfo) throws Exception {
        Vertx vertx = Vertx.vertx();
        // 创建tcp客户端
        NetClient client = vertx.createNetClient();
        CompletableFuture<RpcResponse> cf = new CompletableFuture<>();
        // 建立tcp连接
        client.connect(serviceMetaInfo.getPort(), serviceMetaInfo.getHost(), result -> {
            if (result.succeeded()) { // 发送Rpc请求
                log.debug("TCP server连接成功");
                NetSocket socket = result.result();
                // 封装Rpc请求首部
                ProtocolMessage.Header header = ProtocolMessage.Header.builder()
                        .magicNum(ProtocolConstant.PROTOCOL_MAGIC)
                        .version(ProtocolConstant.PROTOCOL_VERSION)
                        .serializerNum((byte) Objects.requireNonNull(ProtocolMessageSerializerEnum
                                        .getByValue(RpcApplication.getRpcConfig().getSerializer()))
                                .getKey())
                        .messageType((byte) ProtocolMessageTypeEnum.REQUEST.getValue())
                        .requestId(IdUtil.getSnowflakeNextId())
                        .build();
                ProtocolMessage<RpcRequest> requestProtocolMessage = new ProtocolMessage<>(header, rpcRequest);

                try { // 发送Rpc请求
                    Buffer encode = ProtocolMessageEncoder.encode(requestProtocolMessage);
                    socket.write(encode);
                } catch (IOException e) {
                    throw new RuntimeException("协议消息编码错误");
                }

                socket.handler(new TcpBufferHandlerWrapper(buffer -> {
                    try {
                        var decode = (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decode(buffer);
                        cf.complete(decode.getBody());
                    } catch (IOException e) {
                        throw new RuntimeException("协议消息译码错误");
                    }
                }));
            } else {
                Log.error("TCP server连接失败");
            }
        });
        RpcResponse response = cf.get();
        // 关闭Tpc连接
        client.close();
        return response;
    }
}