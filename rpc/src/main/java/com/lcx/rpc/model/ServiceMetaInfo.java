package com.lcx.rpc.model;

import cn.hutool.core.util.StrUtil;
import com.lcx.rpc.config.RpcApplication;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceMetaInfo {
    /**
     * 服务名称
     */
    @Builder.Default
    private String name = "service";
    /**
     * 服务版本
     */
    @Builder.Default
    private String version = "1.0.0";
    /**
     * 服务地址
     */
    @Builder.Default
    private String host = "127.0.0.1";
    /**
     * 服务端口
     */
    @Builder.Default
    private Integer port = 8080;
    /**
     * 服务分组
     */
    @Builder.Default
    private String group = "default";
    /**
     * 权重
     */
    @Builder.Default
    private Integer weight = 1;

    /**
     * 获取服务键
     */
    public String getServiceKey() {
        return String.format("%s/%s:%s", RpcApplication.getRpcConfig().getEnv(), name, version);
    }

    /**
     * 获取服务节点键
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

    @Override
    public boolean equals(Object o) {
        if (o instanceof ServiceMetaInfo serviceMetaInfo) {
            return this.getServiceNodeKey().equals(serviceMetaInfo.getServiceNodeKey());
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, host, port, group, weight);
    }
}
