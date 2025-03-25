package com.qiang.domain.DO.userTeam;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTeamAddDO {
    private Long userId;
    private Long teamId;
}
