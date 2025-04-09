package com.lcx.rpc.bootstrap.config;

import lombok.Data;

/**
 * Rpc客户端配置
 */
@Data
public class ClientConfig {

    /**
     * I/O线程数
     */
    private int ioThreads = Runtime.getRuntime().availableProcessors();

    /**
     * 连接超时时间
     */
    private int connectTimeout = 3000;

}
