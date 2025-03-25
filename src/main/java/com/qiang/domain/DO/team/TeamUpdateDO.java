package com.qiang.domain.DO.team;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamUpdateDO {
    /**
     * 队伍的id
     */
    private Long id;
    /**
     * 队伍的名称
     */
    private String teamName;
    /**
     * 队伍的描述信息
     */
    private String description;
    /**
     * 过期时间
     */
    private LocalDateTime expireTime;
    /**
     * 队伍的状态：公开、私密、加密
     */
    private Integer status;
    /**
     * 队伍房间的密码
     */
    private String password;
}
