package com.lcx.rpc.common.model;

import com.lcx.rpc.bootstrap.config.MyRpcApplication;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceMetaInfo {
    /**
     * 服务名称(接口名称)
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
     * 是否都幂等
     */
    @Builder.Default
    private Boolean idempotent = false;
    /**
     * 细粒度幂等性标注
     */
    private Map<String, Boolean> idempotentMap;

    /**
     * 获取服务键
     */
    public String getServiceKey() {
        return String.format("%s/%s:%s", MyRpcApplication.getRpcConfig().getEnv(), name, version);
    }

    /**
     * 获取服务节点键
     */
    public String getServiceNodeKey() {
        return String.format("%s/%s:%s", getServiceKey(), host, port);
    }

    /**
     * 获取服务地址
     */
    public String getServiceAddress() {
        return String.format("%s:%s", host, port);
    }

    /**
     * 方法是否可重试
     * @param methodName 方法名称
     * @return 是否可重试
     */
    public boolean retryable(String methodName) {
        return idempotent || idempotentMap.getOrDefault(methodName, false);
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
