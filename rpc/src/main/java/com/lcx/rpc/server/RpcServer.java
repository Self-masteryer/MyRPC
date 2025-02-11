package com.lcx.rpc.server;

import com.lcx.rpc.server.handler.RpcReqHandler;

/**
 * rpc服务接口
 */
public interface RpcServer {

    /**
     * 启动服务器
     *
     * @param port          端口
     * @param rpcReqHandler Rpc请求处理器
     */
    void doStart(int port, RpcReqHandler rpcReqHandler);
}