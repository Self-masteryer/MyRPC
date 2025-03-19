package com.lcx.rpc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务注册信息
 * @param <T> 接口
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRegisterInfo<T> {

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 实现类
     */
    private Class<? extends T> implClass;

    /**
     * 能否重试：接口是否具备幂等性
     */
    @Builder.Default
    private Boolean canRetry = false;

}
