package com.qiang.domain.DO.team;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
public class TeamListDO {
    /**
     * 队伍的id
     */
    private Long teamId;
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
     * 队伍的队长用户id
     */
    private Long captainId;
    /**
     * 队伍的状态：公开、私密、加密
     */
    private Integer status;
    /**
     * 队伍的过期时间
     */
    private LocalDateTime expireTime;
    /**
     * 关键词搜索
     */
    private String searchText;
    /**
     * 队伍的id列表
     * 作用：用于查询用户已经加入的队伍，这个列表就是存放用户已经加入的队伍列表
     */
    private Set<Long> teamIdSet;

    /**
     * 分页参数的起始下标
     */
    private Integer offset;
    /**
     * 分页参数的页大小
     */
    private Integer size;

}
