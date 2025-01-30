package com.lcx.rpc.fault.tolerant;

import com.lcx.rpc.config.RpcApplication;
import com.lcx.rpc.spi.SpiLoader;

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
    public static final TolerantStrategy tolerantStrategy = getTolerantStrategy(RpcApplication.getRpcConfig().getTolerantStrategy());

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
