package com.qiang.domain.DTO.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String userAccount;
    private String avatarUrl;//头像
    private Integer gender;
    private String phone;
    private String email;
    private Integer userStatus;
    private Integer userRole;//用户权限 0--普通用户   1--管理员
    private String planetCode;
    private String tags;
    private String profile;//个人简介
    private String createTime;//创建时间
}
