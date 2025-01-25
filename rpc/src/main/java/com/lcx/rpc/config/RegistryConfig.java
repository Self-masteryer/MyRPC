package com.lcx.rpc.config;

import com.lcx.rpc.register.RegistryKeys;
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
    // 超时时间(默认5s)
    private Long timeout = 5000L;
    // 租约时间
    private Integer leaseTime = 30;

}