package com.lcx.rpc.bootstrap.config;

import com.lcx.rpc.protocol.serializer.SerializerKeys;
import lombok.Data;

/**
 * Rpc协议配置
 */
@Data
public class ProtocolConfig {
    /**
     * 序列化器
     */
    private String serializer = SerializerKeys.KRYO;
}
