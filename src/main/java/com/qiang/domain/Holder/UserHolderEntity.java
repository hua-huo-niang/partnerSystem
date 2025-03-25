package com.qiang.domain.Holder;

import lombok.Data;

/**
 * 这个实体类用于存储当前用户的标识信息
 * 不提供详细信息，只提供基本的标识信息
 */
@Data
public class UserHolderEntity {
    /**
     * 用户id
     */
    private Long id;
    /**
     * 用户账号
     */
    private String userAccount;
    /**
     * 用户权限
     */
    private Integer userRole;
}
