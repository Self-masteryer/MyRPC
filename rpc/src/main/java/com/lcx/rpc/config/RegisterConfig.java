package com.lcx.rpc.config;

import com.lcx.rpc.register.RegisterKeys;
import lombok.Data;

/**
 * Rpc注册中心配置
 */
@Data
public class RegisterConfig {
    // 注册中心类别
    private String registry = RegisterKeys.ETCD;
    // 注册中心地址
    private String address = "http://localhost:2379";
    // 用户名
    private String username;
    // 密码
    private String password;
    // 超时时间(默认5s)
    private Long timeout = 5000L;

}