package com.lcx.rpc.protocol.enums;
 
import lombok.Getter;
 
/**
 * 协议类型枚举
 */
@Getter
public enum ProtocolMessageTypeEnum {

    REQUEST(0),    // 请求
    RESPONSE(1),   // 响应
    HEART_BEAT(2), // 心跳
    OTHERS(3);     // 其他

    private final int value;

    ProtocolMessageTypeEnum(int value) {
        this.value = value;
    }

    /**
     * 根据value获取枚举
     */
    public static ProtocolMessageTypeEnum getByValue(int value) {
        for (ProtocolMessageTypeEnum typeEnum : ProtocolMessageTypeEnum.values()) {
            if (typeEnum.value == value) {
                return typeEnum;
            }
        }
        return null;
    }
 
}