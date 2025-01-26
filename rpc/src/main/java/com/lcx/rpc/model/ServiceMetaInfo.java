package com.lcx.rpc.model;

import cn.hutool.core.util.StrUtil;
import com.lcx.rpc.config.RpcApplication;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceMetaInfo {
    /**
     * 服务名称
     */
    private String name = "service";
    /**
     * 服务版本
     */
    private String version = "1.0.0";
    /**
     * 服务地址
     */
    private String host = "127.0.0.1";
    /**
     * 服务端口
     */
    private Integer port = 8080;
    /**
     * 服务分组
     */
    private String group = "default";
    /**
     * 权重
     */
    private Integer weight = 1;

    /**
     * 获取服务键名
     */
    public String getServiceKey() {
        return String.format("%s:%s:%s", RpcApplication.getRpcConfig().getEnv(), name, version);
    }

    /**
     * 获取服务节点键名
     */
    public String getServiceNodeKey() {
        return String.format("%s/%s:%s", getServiceKey(), host, port);
    }

    /**
     * 获取完整服务地址
     */
    public String getServiceAddress() {
        if (!StrUtil.contains(host, "http")) {
            return String.format("http://%s:%s", host, port);
        }
        return String.format("%s:%s", host, port);
    }
}
