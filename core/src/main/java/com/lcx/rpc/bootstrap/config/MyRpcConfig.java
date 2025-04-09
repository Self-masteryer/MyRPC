package com.lcx.rpc.bootstrap.config;

import lombok.Data;

/**
 * 全局配置文件
 */
@Data
public class MyRpcConfig {

    /**
     * 环境
     */
    private String env = "dev";
    /**
     * 名称
     */
    private String name = "service";
    /**
     * 版本号
     */
    private String version = "1.0.0";
    /**
     * 权重
     */
    private Integer weight = 1;
    /**
     * mock
     */
    private boolean mock = false;
    /**
     * 集群
     */
    private ClusterConfig cluster = new ClusterConfig();
    /**
     * 协议
     */
    private ProtocolConfig protocol = new ProtocolConfig();
    /**
     * 服务器
     */
    private ServerConfig server = new ServerConfig();
    /**
     * 客户端
     */
    private ClientConfig client = new ClientConfig();

}
