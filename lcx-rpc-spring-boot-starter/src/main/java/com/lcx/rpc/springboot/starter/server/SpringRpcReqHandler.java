package com.lcx.rpc.springboot.starter.server;

import com.lcx.rpc.server.handler.RpcReqHandler;
import com.lcx.rpc.springboot.starter.utils.SpringContextUtil;

public class SpringRpcReqHandler implements RpcReqHandler {

    @Override
    public Object getInstance(Class<?> clazz) {
        return SpringContextUtil.getBean(clazz);
    }

}
