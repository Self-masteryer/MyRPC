package com.lcx.rpc.bootstrap.config;

import lombok.Data;

/**
 * Rpc服务器配置
 */
@Data
public class ServerConfig {

    /**
     * 服务器主机名
     */
    private String host = "localhost";
    /**
     * 服务器端口
     */
    private Integer port = 8080;
    /**
     * Reactor线程模型
     */
    private Group group = new Group();
    /**
     * 服务器参数配置
     */
    private Option option = new Option();

    @Data
    public static class Group {
        /**
         * 主Reactor数量：处理连接事件
         */
        private int bossThreads = 1;

        /**
         * 从Reactor数量：处理读写事件
         */
        private int workerThreads = Runtime.getRuntime().availableProcessors();

        /**
         * 业务线程数量：执行业务
         */
        private int businessThreads = 32;
    }

    @Data
    public static class Option {
        /**
         * 连接队列大小
         */
        private int soBacklog = 1024;
    }

}