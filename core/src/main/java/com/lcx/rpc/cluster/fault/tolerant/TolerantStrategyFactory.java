package com.lcx.rpc.cluster.fault.tolerant;

import com.lcx.rpc.bootstrap.config.MyRpcApplication;
import com.lcx.rpc.common.spi.SpiLoader;

/**
 * 容错策略工厂
 */
public class TolerantStrategyFactory {

    static {
        SpiLoader.load(TolerantStrategy.class);
    }

    /**
     * 配置的容错策略
     */
    public static final TolerantStrategy tolerantStrategy = getTolerantStrategy(MyRpcApplication.getRpcConfig().getCluster().getTolerantStrategy());

    /**
     * 获取容错策略实例
     *
     * @param key 键值
     * @return 容错策略实例
     */
    public static TolerantStrategy getTolerantStrategy(String key) {
        return SpiLoader.getInstance(TolerantStrategy.class, key);
    }

}
