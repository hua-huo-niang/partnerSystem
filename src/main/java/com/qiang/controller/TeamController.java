package com.qiang.controller;

import com.qiang.comment.Result;
import com.qiang.domain.request.team.*;
import com.qiang.service.TeamService;
import com.qiang.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/team")
public class TeamController {
    @Autowired
    private TeamService teamService;
    /**
     * 增
     * @param request 请求实体类
     * @return Result 统一响应结果对象
     */
    @PostMapping("/add")
    public Result addTeam(@RequestBody TeamAddRequest request){
        return teamService.addTeam(request);
    }

    /**
     * 删
     * @param request team的id
     * @return Reuslt 统一响应结果对象
     */
    @DeleteMapping("/delete")
    public Result deleteTeam(@RequestBody TeamDeleteRequest request){
        return teamService.deleteTeam(request.getTeamId());
    }

    /**
     * 改
     * @param request 请求实体对象
     * @return Reuslt 统一响应结果对象
     */
    @PutMapping("/update")
    public Result updateTeam(@RequestBody TeamUpdateRequest request){
        return teamService.updateteam(request);
    }

    /**
     * 查
     * @param id team的id
     * @return Reuslt 统一响应结果对象
     */
    @GetMapping("/get")
    public Result getTeam(@RequestParam Long id){
        return teamService.getTeam(id);
    }

    /**
     * 根据传入的参数去查询队伍列表
     * @param request 查询队伍列表业务的请求封装对象
     * @return Result 统一响应结果对象
     */
    @GetMapping("/list")
    public Result listTeam(TeamListRequest request){
        return teamService.listTeambyPage(request);
    }

    /**
     * 根据传入的参数去加入队伍
     * @param request 加入队伍列表业务的请求封装对象
     * @return Result 统一响应结果对象
     */
    @PostMapping("/join")
    public Result joinTeam(@RequestBody TeamJoinRequest request){
        return teamService.joinTeam(request);
    }

    @PostMapping("/quit")
    public Result quitTeam(@RequestBody TeamQuitRequest request){
        return teamService.quitTeam(request);
    }


    /**
     * 根据传入的参数，列出用户是队长的队伍
     * @param request request 查询队伍业务的请求封装对象
     * @return Result 统一响应结果对象
     */
    @GetMapping("/list/my/create")
    public Result listMyCreatedTeam(TeamListRequest request){
        return teamService.listMyCreatedTeam(request);
    }



    /**
     * 根据传入的参数，列出用户已经加入的所有队伍
     * @param request request 查询队伍业务的请求封装对象
     * @return Result 统一响应结果对象
     */
    @GetMapping("/list/my/join")
    public Result listMyJoinedTeam(TeamListRequest request){
        return teamService.listMyJoinTeamByPage(request);
    }
}
