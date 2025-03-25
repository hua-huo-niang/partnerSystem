package com.qiang.domain.VO.team;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeamGetVO {
    /**
     * id
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
     * 队伍的人数上限
     */
    private Integer maxNum;
    /**
     * 过期时间
     */
    private LocalDateTime expireTime;
    /**
     * 队伍的创建者
     */
    private Long userId;
    /**
     * 队伍的状态：公开、私密、加密
     */
    private Integer status;

}
