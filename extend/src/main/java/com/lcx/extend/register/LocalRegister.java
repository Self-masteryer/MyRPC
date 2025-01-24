package com.lcx.extend.register;

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
     * @param interfaceName 服务名称
     * @param implClass 服务实例class类
     */
    public static void register(String interfaceName,Class<?> implClass){
        map.put(interfaceName,implClass);
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
