package com.qiang.domain.DTO.userTeam;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserTeamGetDTO {
    private Long id;
    private Long userId;
    private Long teamId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
