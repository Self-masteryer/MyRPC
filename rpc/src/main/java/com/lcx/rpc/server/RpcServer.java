package com.lcx.rpc.server;

/**
 * rpc服务接口
 */
public interface RpcServer {

    /**
     * 启动服务器
     *
     * @param port 端口
     */
    void doStart(int port);
}