package com.lcx.rpc.config;

import com.lcx.rpc.serializer.SerializerKeys;
import lombok.Data;

/**
 * 全局配置文件
 */
@Data
public class RpcConfig {

    /**
     * 名称
     */
    private String name = "MyRPC";

    /**
     * 版本号
     */
    private String version = "1.0.0";

    /**
     * 服务器主机名
     */
    private String host = "localhost";

    /**
     * 服务器端口
     */
    private Integer port = 8080;

    /**
     * mock
     */
    private boolean mock = false;

    /**
     * 序列化器
     */
    private String serializer = SerializerKeys.KRYO;

    /**
     * 注册中心
     */
    private RegistryConfig registry = new RegistryConfig();
}
