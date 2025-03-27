package com.qiang.service;

import com.qiang.comment.Result;
import com.qiang.domain.request.team.*;

public interface TeamService {

    Result addTeam(TeamAddRequest teamRequest);

    Result deleteTeam(Long id);

    Result updateteam(TeamUpdateRequest teamRequest);

    Result getTeam(Long id);

    Result listTeambyPage(TeamListRequest request);

    Result joinTeam(TeamJoinRequest request);

    Result quitTeam(TeamQuitRequest request);

    Result listMyJoinTeamByPage(TeamListRequest request);

    Result listMyCreatedTeam(TeamListRequest request);
}
