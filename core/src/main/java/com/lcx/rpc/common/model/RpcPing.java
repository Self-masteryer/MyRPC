package com.lcx.rpc.common.model;

import lombok.Data;

@Data
public class RpcPing {

    /**
     * 序列号，用于追踪请求-响应配对，避免重复或丢失导致的误判
     */
    int sequenceId;

    /**
     * 发送时间戳（毫秒），用于计算网络延迟及判断响应超时
     */
    long timestamp;

}
