package com.lcx.rpc.protocol.enums;
 
import lombok.Getter;
 
/**
 * 协议状态枚举类
 */
@Getter
public enum ProtocolMessageStatusEnum {

    OK("ok",200),
    BAD_REQUEST("bad request",400),
    BAD_RESPONSE("bad response",500);

    private  final String text;
    private final int value;
 
    ProtocolMessageStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据状态码获取枚举
     */
    public static ProtocolMessageStatusEnum getByValue(int value){
        for(ProtocolMessageStatusEnum statusEnum : ProtocolMessageStatusEnum.values()){
            if(statusEnum.value == value){
                return statusEnum;
            }
        }
        return null;
    }

}