package com.lcx.rpc.bootstrap.config;

import com.lcx.rpc.cluster.register.RegistryKeys;
import lombok.Data;

/**
 * Rpc注册中心配置
 */
@Data
public class RegistryConfig {
    // 注册中心类别
    private String type = RegistryKeys.ETCD;
    // 注册中心地址
    private String address = "http://localhost:2379";
    // 用户名
    private String username;
    // 密码
    private String password;
    // 连接超时时间(默认5s)
    private Long timeout = 5000L;
    // 租约时间(默认30秒),续约频率=租约时间/3
    private Integer leaseTime = 30;
    // 补偿间隔(默认3分钟)
    private Integer compensationInterval = 3;
}