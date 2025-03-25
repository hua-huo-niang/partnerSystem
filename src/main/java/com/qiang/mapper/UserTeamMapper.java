package com.qiang.mapper;

import com.qiang.domain.DO.userTeam.UserTeamAddDO;
import com.qiang.domain.DTO.UserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserTeamMapper {

    /**
     * 根据UserTeamDO插入一条新数据到user_team表中
     * @param userTeamAddDO 数据库user_team表交互对象
     * @return Integer 改变的行数
     */
    Integer addOne(@Param("userTeam") UserTeamAddDO userTeamAddDO);

    /**
     * 根据用户id和队伍的id去查找
     * 用户是否已经加入了队伍中
     * @param userId 用户id
     * @param teamId 队伍id
     * @return Integer
     */
    Integer isHashJoin(@Param("userId") Long userId, @Param("teamId") Long teamId);

    /**
     * 根据队伍的id，查询队伍中已经有多少人加入
     * @param teamId 队伍id
     * @return Integer 加入队伍中的人数
     */
    Integer countTeamHashJoin(@Param("teamId") Long teamId);

    /**
     * 根据用户的id，查询用户已经加入了多少个队伍
     * @param userId 用户id
     * @return Integer 用户已经加入的队伍
     */
    Integer countUserJoinTeam(@Param("userId") Long userId);

    /**
     * 根据teamId 将 user_team 表中的有关数据（于teamId的相关数据）删除
     * @param teamId
     */
    void deleteOneTeam(@Param("teamId") Long teamId);

    /**
     * 根据队伍的id去查询所有已经入队伍的用户的信息
     * @param teamId 队伍的id
     * @return List<UserDTO> 所有已加入队伍的用户列表
     */
    List<UserDTO> getAllJoinUser(@Param("teamId") Long teamId);

    /**
     * 根据用户id和队伍id进行删除user_team中的相关数据记录
     * @param userId 用户id
     * @param teamId 队伍id
     * @return Integer 改变的行数
     */
    Integer quitTeam(@Param("userId") Long userId, @Param("teamId") Long teamId);
}
