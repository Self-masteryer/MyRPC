package com.lcx.rpc.server;

/**
 * rpc服务接口
 */
public interface IRpcServer {
    /**
     * 启动服务器
     * @param port
     */
    void doStart(int port);
}