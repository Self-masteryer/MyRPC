package com.lcx.rpc.cluster.fault.restrictor;

/**
 * 限流器
 */
public interface Restrictor {

    /**
     * 获取访问许可
     * @return 是否允许访问
     */
    boolean getToken();
}
