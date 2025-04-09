package com.lcx.rpc.common.model;

import lombok.Data;

@Data
public class RpcPong {

    /**
     * 回显Ping的sequenceId
     */
    int sequenceId;

    /**
     * 回显Ping的时间戳
     */
    long timestamp;

    /**
     * 服务器负载信息
     */
    ServerLoad serverLoad;

    static class ServerLoad {

    }

}
