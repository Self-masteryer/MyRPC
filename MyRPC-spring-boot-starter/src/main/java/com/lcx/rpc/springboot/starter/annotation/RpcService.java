package com.lcx.rpc.springboot.starter.annotation;

import com.lcx.rpc.common.constant.RpcConstant;
import com.lcx.rpc.springboot.starter.utils.SpringContextUtil;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Service
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({SpringContextUtil.class})
public @interface RpcService {
    /**
     * 服务接口类
     */
    Class<?> interfaceClass() default void.class;

    /**
     * 服务版本
     */
    String serviceVersion() default RpcConstant.DEFAULT_SERVICE_VERSION;
}
