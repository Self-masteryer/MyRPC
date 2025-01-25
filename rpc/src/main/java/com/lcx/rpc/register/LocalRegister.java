package com.lcx.rpc.register;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地服务注册中心
 */
public class LocalRegister {

    // 存储注册信息
    private static final Map<String,Class<?>> map = new ConcurrentHashMap<>();

    /**
     * 注册服务
     * @param serviceName 服务名称
     * @param clazz 服务实例class类
     */
    public static void register(String serviceName,Class<?> clazz){
        map.put(serviceName,clazz);
    }

    /**
     * 服务发现
     */
    public static Class<?> get(String interfaceName){
        return map.get(interfaceName);
    }

    /**
     * 删除服务
     */
    public static void delete(String interfaceName){
        map.remove(interfaceName);
    }
}
