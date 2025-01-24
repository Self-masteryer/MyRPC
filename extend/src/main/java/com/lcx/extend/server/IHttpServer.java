package com.lcx.extend.server;
/**
 * Http服务接口
 */
public interface IHttpServer {
    /**
     * 启动服务器
     * @param port
     */
    void doStart(int port);
}