package com.qiang.util;

import lombok.Getter;

@Getter
public enum ErrorCode {

    SUCCESS(2000,"操作成功"),
    ERROR_PARAMS(4001,"参数异常"),
    ERROR_STATUS(4002,"用户状态异常"),
    ERROR_USER_OPTIONS(4003,"用户操作异常"),
    ERROR_AUTH(4004,"无权限"),
    ERROR_USER(4005,"用户错误"),
    ERROR_SYSTEM(5000,"系统异常"),
    ERROR_DATABASE(50001,"数据库异常"),
    ERROR_OTHER(500,"出异常了")
    ;


    private final Integer code;
    private final String messgae;
    ErrorCode(Integer code, String message){
        this.code = code;
        this.messgae = message;
    }


}
