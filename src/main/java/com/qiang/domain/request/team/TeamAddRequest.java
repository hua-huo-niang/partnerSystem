package com.qiang.domain.request.team;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
/**
 * TeamController控制器的请求实体类
 * 作用：专门用来获取请求信息
 */
public class TeamAddRequest {
    /**
     * 队伍的名称
     */
    private String name;
    /**
     * 队伍的描述信息
     */
    private String description;
    /**
     * 队伍的人数上限
     */
    private Integer maxNum;
    /**
     * 过期时间
     */
    private LocalDateTime expireTime;
    /**
     * 队伍的队长用户id
     */
    private Long userId;
    /**
     * 队伍的状态：公开、私密、加密
     */
    private Integer status;
    /**
     * 队伍房间的密码
     */
    private String password;
}
