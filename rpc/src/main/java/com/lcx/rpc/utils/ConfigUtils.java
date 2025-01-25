package com.lcx.rpc.utils;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;

import java.util.Objects;

/**
 * 配置工具类：加载配置文件
 */
public class ConfigUtils {

    private static final String[] SUFFIXES = {".properties", ".yml", ".yaml"};

    public static <T> T loadConfig(Class<T> clazz,String prefix){
        return loadConfig(clazz,prefix,"");
    }

    public static <T> T loadConfig(Class<T> clazz, String prefix, String env) {
        Objects.requireNonNull(clazz, "Config class must not be null");
        try {
            String fileName = buildConfigFileName(env);
            Props props = new Props(fileName);
            return props.toBean(clazz, prefix);
        } catch (Exception e) {
            throw new IllegalStateException("Load config failed: " + env, e);
        }
    }

    private static String buildConfigFileName(String env) {
        String baseName = StrUtil.isBlank(env) ?
                "application" :
                "application-" + env.trim();

        for (String suffix : SUFFIXES) {
            String fullName = baseName + suffix;
            if (ResourceUtil.getResource(fullName) != null) {
                return fullName;
            }
        }
        throw new IllegalArgumentException("No config file found for: " + baseName);
    }

}
