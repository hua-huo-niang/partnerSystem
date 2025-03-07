package com.qiang.domain.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String userAccount;
    private String avaterUrl;//头像
    private Integer userRole;//用户权限 0--普通用户   1--管理员
}
