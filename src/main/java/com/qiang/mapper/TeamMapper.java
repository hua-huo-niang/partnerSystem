package com.qiang.mapper;

import com.qiang.domain.DO.team.TeamAddDO;
import com.qiang.domain.DO.team.TeamListDO;
import com.qiang.domain.DO.team.TeamUpdateDO;
import com.qiang.domain.DTO.TeamGetDTO;
import com.qiang.domain.DTO.TeamListDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TeamMapper {

    /**
     * 根据TeamAddRequest实体类添加一个队伍
     * @param team 实体类
     * @return Integer 改变的行数
     */
    Integer addOneTeam(TeamAddDO team);

    /**
     * 根据id去获取队伍的数量
     * 判断是否存在这个队伍
     * @param id 队伍id
     * @return Integer 队伍的数量
     */
    Integer countTeamByTeamId(@Param("id") Long id);

    /**
     * 根据队伍id去删除一个队伍
     * @param id 队伍的id
     * @return Integer 改变的行数
     */
    Integer deleteOneTeam(@Param("id") Long id);

    /**
     * 根据TeamUpdateRequest实体类去修改队伍信息
     * @param teamUpdateDO 实体类
     * @return  Integer 改变的行数
     */
    Integer updateOneTeam(@Param("team") TeamUpdateDO teamUpdateDO);

    /**
     * 根据id获取一个队伍的信息
     *
     * @param id 队伍的id
     * @return TeamDTO team的响应实体类
     */
    TeamGetDTO getOneTeam(@Param("id") Long id);

    /**
     * 根据userId去查询队伍的数量，看看当前用户创建了多少个队伍
     * @param userId 用户的id
     * @return Integer 队伍的数量
     */
    Integer countTeamByUserId(@Param("userId") Long userId);

    /**
     * 根据传入的teamListDO条件参数，查询队伍的列表
     * 使用一次性查询的方法
     * @param teamListDO 查询队伍列表条件参数实体类
     * @return List<TeamListDTO> TeamListDTO结果列表
     */
    List<TeamListDTO> listTeamByCondition(TeamListDO teamListDO);

    /**
     * 根据传入的teamListDO条件参数，查询队伍的列表
     * 使用分步查询的方法
     * @param teamListDO 查询队伍列表条件参数实体类
     * @return List<TeamListDTO> TeamListDTO结果列表
     */
    List<TeamListDTO> listTeamByCondition2(TeamListDO teamListDO);

    /**
     * 根据传入用户id更新队伍的队长id
     * @param nextCaptainId 下一任队长的id
     * @param teamId 队伍的id
     * @return Integer 改变的行数
     */
    Integer updateCaptainId(@Param("nextCaptainId") Long nextCaptainId,@Param("teamId") Long teamId);
}
