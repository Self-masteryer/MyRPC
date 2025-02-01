package com.lcx.rpc.server;

import io.vertx.core.Handler;

/**
 * rpc服务接口
 */
public interface RpcServer<T> {

    /**
     * 启动服务器
     * @param port 端口
     * @param handler 处理器
     */
    void doStart(int port, Handler<T> handler);
}