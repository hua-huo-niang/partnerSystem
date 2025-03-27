package com.qiang.domain.DTO.team;

import com.qiang.domain.DTO.user.UserDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TeamListDTO {
    /**
     * 队伍的id
     */
    private Long teamId;
    /**
     * 队伍名称
     */
    private String teamName;
    /**
     * 队伍的描述
     */
    private String description;
    /**
     * 过期时间
     */
    private LocalDateTime expireTime;
    /**
     * 最大人数
     */
    private Integer maxNum;
    /**
     * 队伍的状态
     */
    private Integer status;
    /**
     * 创造者的id
     */
    private Long creatorId;
    /**
     * 队长的id
     */
    private Long captainId;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 当前队伍已经加入的人数
     */
    private Integer hasJoinNum;
    /**
     * 当前用户是否已经加入
     */
    private Boolean hasJoin;




    /**
     * 队伍中已经加入的用户信息
     */
    private List<UserDTO> joinUsers;
    /**
     * 队长
     */
    private UserDTO captain;
    /**
     * 创建着
     */
    private UserDTO creator;
}
