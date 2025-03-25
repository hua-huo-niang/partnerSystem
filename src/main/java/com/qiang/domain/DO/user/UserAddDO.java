package com.qiang.domain.DO.user;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAddDO {
    private  Long id;
    @ExcelProperty("username")
    private String username;

    @ExcelProperty("userAccount")
    private String userAccount;

    @ExcelProperty("userPassword")
    private String userPassword;

    @ExcelProperty("avatarUrl")
    private String avatarUrl;

    @ExcelProperty("gender")
    private Integer gender;

    @ExcelProperty("phone")
    private String phone;

    @ExcelProperty("email")
    private String email;

    @ExcelProperty("userRole")
    private Integer userRole;

    @ExcelProperty("planetCode")
    private String planetCode;

    @ExcelProperty("tags")
    private String tags;

    @ExcelProperty("profile")
    private String profile;

}
