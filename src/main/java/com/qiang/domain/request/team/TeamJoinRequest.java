package com.qiang.domain.request.team;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamJoinRequest {
    /**
     * 队伍的id
     */
    private Long teamId;
    /**
     * 队伍的密码
     */
    private String password;
}
