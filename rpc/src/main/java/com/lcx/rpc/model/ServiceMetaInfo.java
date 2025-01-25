package com.lcx.rpc.model;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceMetaInfo {
    /**
     * 服务名称
     */
    private String serviceName = "service";
    /**
     * 服务版本
     */
    private String serviceVersion = "1.0.0";
    /**
     * 服务地址
     */
    private String serviceHost = "127.0.0.1";
    /**
     * 服务端口
     */
    private Integer servicePort = 8080;
    /**
     * 服务分组
     */
    private String serviceGroup = "default";

    /**
     * 获取服务键名
     */
    public String getServiceKey() {
        return String.format("%s:%s", serviceName, serviceVersion);
    }

    /**
     * 获取服务节点键名
     */
    public String getServiceNodeKey() {
        return String.format("%s/%s:%s", getServiceKey(), serviceHost, servicePort);
    }

    /**
     * 获取完整服务地址
     */
    public String getServiceAddress() {
        if (!StrUtil.contains(serviceHost, "http")) {
            return String.format("http://%s:%s", serviceHost, servicePort);
        }
        return String.format("%s:%s", serviceHost, servicePort);
    }
}
