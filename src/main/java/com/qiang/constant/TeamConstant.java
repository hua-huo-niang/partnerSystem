package com.qiang.constant;

public class TeamConstant {
    /**
     * team业务中，加入队伍的分布式锁
     */
    public static final String LOCK_TEAM_JOINTEAM = "LOCK:TEAM:JOINTEAM:";
    /**
     * team业务中，加入队伍的分布式锁，的过期时间
     */
    public static final Long LOCK_TEAM_JOINTEAM_TTL = 5L;

    public static final Integer MAX_USER_JOINTEAM = 5;
}
