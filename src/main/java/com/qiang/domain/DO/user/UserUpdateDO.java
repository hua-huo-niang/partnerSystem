package com.qiang.domain.DO.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDO {
    private Long id;
    private String username;
    private String avatarUrl;//头像
    private Integer gender;
    private String phone;
    private String email;
    private String tags;
    private String profile;//个人简介
}
