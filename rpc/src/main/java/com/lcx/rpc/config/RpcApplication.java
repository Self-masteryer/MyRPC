package com.lcx.rpc.config;

import com.lcx.rpc.utils.ConfigUtils;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
public class RpcApplication {

    private RpcConfig rpc;
    private static volatile RpcApplication rpcApplication;
    private static final Logger log = LoggerFactory.getLogger(RpcApplication.class);

    public static RpcConfig getRpcConfig() {
        if (rpcApplication == null) {
            synchronized (RpcApplication.class) {
                if (rpcApplication == null) {
                    init();
                }
            }
        }
        return rpcApplication.getRpc();
    }

    private static void init() {
        try {
            // 加载配置文件
            RpcApplication.rpcApplication = ConfigUtils.loadConfig(RpcApplication.class, "");
        } catch (Exception e) {
            // 加载失败，采用默认值
            RpcApplication.rpcApplication = new RpcApplication();
        }
    }
}
