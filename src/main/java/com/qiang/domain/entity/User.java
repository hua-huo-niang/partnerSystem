package com.qiang.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private  Long id;
    private String username;
    private String userAccount;
    private String userPassword;
    private String avaterUrl;
    private Integer gender;
    private String phone;
    private String email;
    private Integer userStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDelete;
    private Integer userRole;
    private String plantCode;
}
