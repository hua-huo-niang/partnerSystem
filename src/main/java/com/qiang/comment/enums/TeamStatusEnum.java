package com.qiang.comment.enums;


import lombok.Getter;

@Getter
public enum TeamStatusEnum {
    PUBLIC(0,"公开"),
     PRIVATE(1,"私密"),
    ENCRYPT(2,"加密");

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 状态的描述
     */
    private final String description;
    TeamStatusEnum(Integer status,String description){
        this.status = status;
        this.description = description;
    }

     public static TeamStatusEnum getEnumByValue(Integer value){
        if (value==null){
            return null;
        }
        TeamStatusEnum[] values = TeamStatusEnum.values();
        for (TeamStatusEnum teamStatusEnum : values) {
            if (teamStatusEnum.getStatus().equals(value)) {
                return teamStatusEnum;
            }
        }
        return null;
    }

}
