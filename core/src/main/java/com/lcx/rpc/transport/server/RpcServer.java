package com.lcx.rpc.transport.server;

/**
 * rpc服务接口
 */
public interface RpcServer {

    /**
     * 启动服务器
     *
     * @param port 端口
     */
    void start(int port);
}