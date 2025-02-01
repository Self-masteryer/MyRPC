package com.lcx.rpc.springboot.starter.annotation;

import com.lcx.rpc.springboot.starter.bootstrap.RpcConsumerBootStrap;
import com.lcx.rpc.springboot.starter.bootstrap.RpcInitBootStrap;
import com.lcx.rpc.springboot.starter.bootstrap.RpcProviderBootStrap;
import com.lcx.rpc.springboot.starter.utils.SpringContextUtil;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启用Rpc注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({RpcInitBootStrap.class, RpcConsumerBootStrap.class, RpcProviderBootStrap.class})
public @interface EnableRpc {

    /**
     * 是否启动服务器
     */
    boolean needServer() default true;

}
