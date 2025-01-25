package com.lcx.rpc.config;

import com.lcx.rpc.utils.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcApplication {

    private static volatile RpcConfig rpcConfig;
    private static final Logger log = LoggerFactory.getLogger(RpcApplication.class);

    public static RpcConfig getRpcConfig() {
        if(rpcConfig == null) {
            synchronized (RpcApplication.class) {
                if(rpcConfig == null) {
                    init();
                }
            }
        }
        return rpcConfig;
    }

    private static void init() {
        RpcConfig newRpcConfig = null;
        try {
            // 加载配置文件
            newRpcConfig = ConfigUtils.loadConfig(RpcConfig.class, "");
        } catch (Exception e) {
            // 加载失败，采用默认值
            rpcConfig = new RpcConfig();
        }
        init(newRpcConfig);
    }

    private static void init(RpcConfig rpcConfig) {
        RpcApplication.rpcConfig = rpcConfig;
        log.info("rpc config:{}", rpcConfig);
    }

}
