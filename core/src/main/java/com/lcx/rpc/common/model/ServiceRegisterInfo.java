package com.lcx.rpc.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务注册信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRegisterInfo {

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 实现类
     */
    private Class<?> implClass;

}
