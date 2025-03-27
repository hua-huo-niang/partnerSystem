package com.qiang.service.impl;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.qiang.comment.Result;
import com.qiang.comment.enums.TeamStatusEnum;
import com.qiang.comment.exception.BusinessException;
import com.qiang.domain.DO.team.TeamAddDO;
import com.qiang.domain.DO.team.TeamListDO;
import com.qiang.domain.DO.team.TeamUpdateDO;
import com.qiang.domain.DO.userTeam.UserTeamAddDO;
import com.qiang.domain.DTO.team.TeamGetDTO;
import com.qiang.domain.DTO.user.UserDTO;
import com.qiang.domain.DTO.userTeam.UserTeamGetDTO;
import com.qiang.domain.Holder.UserHolderEntity;
import com.qiang.domain.VO.team.TeamGetVO;
import com.qiang.domain.request.team.*;
import com.qiang.mapper.TeamMapper;
import com.qiang.mapper.UserTeamMapper;
import com.qiang.service.TeamService;
import com.qiang.service.UserTeamService;
import com.qiang.util.UserHolder;
import com.qiang.domain.DTO.team.TeamListDTO;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.qiang.comment.ErrorCode.*;
import static com.qiang.constant.TeamConstant.LOCK_TEAM_JOINTEAM;
import static com.qiang.constant.TeamConstant.MAX_USER_JOINTEAM;
import static com.qiang.constant.UserConstant.ADMINISTRATOR_AUTHORITY;

@Service
@Slf4j
public class TeamServiceImpl implements TeamService {

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private UserTeamMapper userTeamMapper;
    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private UserTeamService userTeamService;

    private static final String lockUUid = UUID.randomUUID().toString(true);


    @Override
    @Transactional
    public Result addTeam(TeamAddRequest request) {
        //1. 是否登录，未登录不能操作（这一步在拦截器中已经配置）
        //2. 参数是否为空 不需要验证，not null的字段只有userId和status，这两个都可以有系统默认值

        //3. 校验信息
        validAddTeamParams(request);

        //创建TeamAddDO,用于与数据库交互的实体类
        TeamAddDO teamAddDO = new TeamAddDO();
        BeanUtil.copyProperties(request, teamAddDO,CopyOptions.create().setFieldMapping(MapUtil.of("name","teamName")));

        //4. 加入队伍到team表中
        Long userId = UserHolder.getUser().getId();
        teamAddDO.setCreatorId(userId);
        teamAddDO.setCaptainId(userId);
        Integer count = teamMapper.addOneTeam(teamAddDO);
        Long teamId = teamAddDO.getId();
        if (count<=0||teamId==null){
            throw new BusinessException(ERROR_PARAMS,"队伍创建失败！");
        }

        //5. 插入userId和teamId到user_team表中
        UserTeamAddDO userTeamAddDO =new UserTeamAddDO(userId,teamId);
        count = userTeamMapper.addOne(userTeamAddDO);
        if (count<=0){
            throw new BusinessException(ERROR_PARAMS,"创建队伍失败！");
        }

        //6. 返回结果
        return Result.ok(SUCCESS,teamId,"操作成功！");
    }




    @Override
    @Transactional
    public Result deleteTeam(Long teamId) {
        if (teamId==null||teamId==0L) {
            throw new BusinessException(ERROR_PARAMS,"请正确选择要删除的队伍！");
        }
        //先查询有没有这个队伍，是否已经被删除
        TeamGetDTO teamGetDTO = teamMapper.getOneTeam(teamId);
        if (teamGetDTO==null||BeanUtil.isEmpty(teamGetDTO)){
            throw new BusinessException(ERROR_PARAMS,"队伍不存在！");
        }
        //只有队伍的队长或者管理员才能进行删除队伍
        UserHolderEntity user = UserHolder.getUser();
        Long userId = user.getId();
        Integer userRole = user.getUserRole();
        Long captainId = teamGetDTO.getCaptainId();
        if (!captainId.equals(userId)&&!userRole.equals(ADMINISTRATOR_AUTHORITY)){//既不是队长，也不是管理员
            throw new BusinessException(ERROR_PARAMS,"权限不足！");
        }
        Integer count  = teamMapper.deleteOneTeam(teamId);
        if (count==null||count==0){
            throw  new BusinessException(ERROR_SYSTEM,"删除队伍失败！");
        }
        //将user_team中的有关数据全部删除
        count= userTeamMapper.deleteOneTeam(teamId);
        if(count==null||count==0){
            throw new BusinessException(ERROR_SYSTEM,"删除队伍失败！");
        }
        return Result.ok(SUCCESS,count,"操作成功");
    }




    @Override
    public Result updateteam(TeamUpdateRequest request) {
        //先查询这个队伍是否存在
        //1. id要有效，如果队伍不存在，修改没有意义
        Integer count = teamMapper.countTeamByTeamId(request.getId());
        if (count==null||count==0){
            throw new BusinessException(ERROR_PARAMS,"该队伍不存在！");
        }
        //2. teamName可以为空，不为空时长度在 2<= length <=20
        String teamName = request.getName();
        if (!StrUtil.isBlank(teamName)&&(teamName.length()<2||teamName.length()>20)){
            throw new BusinessException(ERROR_PARAMS,"队伍的名称字数范围为2~20");
        }
        //3. descrption可以为空，不为空时 0<= length <= 512
        String description = request.getDescription();
        if (!StrUtil.isBlank(description)&&description.length()>512){
            throw new BusinessException(ERROR_PARAMS, "队伍的描述字数范围为0~512");
        }
        //4. expireTime可以为空，空表示查询所有未过期的队伍
        LocalDateTime expireTime = request.getExpireTime();
        if (expireTime.isBefore(LocalDateTime.now())){
            throw new BusinessException(ERROR_PARAMS,"过期时间错误，过期时间必须要大于当前时间！");
        }
        //5. status可以为空，且只能对应枚举类型，
        Integer status = Optional.ofNullable(request.getStatus()).orElse(0);
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if (teamStatusEnum==null) {
            throw new BusinessException(ERROR_PARAMS,"队伍的状态错误！");
        }
        //6. 如果status为加密，那么password不能为空
        String password = request.getPassword();
        if (TeamStatusEnum.ENCRYPT.equals(teamStatusEnum)) {
            if (StrUtil.isBlank(password)){
                throw new BusinessException(ERROR_PARAMS,"设置为加密房间，密码不能为空！");
            }else if (password.length()<4||password.length()>20){
                throw new BusinessException(ERROR_PARAMS,"密码的字数范围为4~20");
            }
        }
        //创建TeamUpdateDO
        TeamUpdateDO teamUpdateDO = new TeamUpdateDO();
        BeanUtil.copyProperties(request,teamUpdateDO, CopyOptions.create().setFieldMapping(MapUtil.of("name","teamName")));
        count  = teamMapper.updateOneTeam(teamUpdateDO);
        if (count==null||count==0){
            throw new BusinessException(ERROR_SYSTEM,"队伍修改失败！");
        }
        return Result.ok(SUCCESS,count,"操作成功！");
    }



    @Override
    public Result getTeam(Long id) {
        TeamGetDTO teamGetDTO = teamMapper.getOneTeam(id);
        if (teamGetDTO==null){
            throw new BusinessException(ERROR_PARAMS,"该队伍不存在！");
        }
        TeamGetVO vo = new TeamGetVO();
        BeanUtil.copyProperties(teamGetDTO,vo,CopyOptions.create().setFieldMapping(MapUtil.of("teamName","name")));
        vo.setUserId(teamGetDTO.getCaptainId());
        return Result.ok(SUCCESS,vo);
    }

    @Override
    public Result listTeambyPage(TeamListRequest request) {
        //1. 从请求参数中取出队伍名称等查询条件，**进行校验**，判断非空，非空则作为条件

        //   - id可以为空，并且不能为0，数据库中id从1开始自增
        //   - teamName不为空，且长度在 2<= length <=20  因为是模糊查询可以短于2，但是不能长于20
        String teamName = request.getName();
        if (teamName!=null&&(teamName.length()>20)){
            throw new BusinessException(ERROR_PARAMS,"队伍的名称的字数范围是2~20");
        }
        //   - descrption不为空，且 0<= length <= 512
        String description = request.getDescription();
        if (description!=null&&(description.length()>512)){
            throw new BusinessException(ERROR_PARAMS,"队伍描述信息的字数范围是0~512");
        }
        //   - maxNum不为空，且 队伍人数：1<= maxNum <=20
        Integer maxNum = request.getMaxNum();
        if (maxNum!=null&&(maxNum<1||maxNum>20)){
            throw new BusinessException(ERROR_PARAMS,"队伍人数的范围是1~20");
        }
        //   - userId不为空，且不能为0，因为数据库中id从1开始自增
        Long userId = request.getUserId();
        if (userId!=null&&userId==0){
            throw new BusinessException(ERROR_PARAMS,"用户的id错误");
        }
        //   - status不为空，且只能对应枚举类型
         Integer status = Optional.ofNullable(request.getStatus()).orElse(0);//如果为空的话，就默认是公开-0
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if (teamStatusEnum==null){
            throw new BusinessException(ERROR_PARAMS,"队伍的状态错误");
        }
        //    - expireTime可以为空，不为空时要大于当前时间
        LocalDateTime expireTime = request.getExpireTime();
        if (expireTime!=null&&expireTime.isBefore(LocalDateTime.now())) {
            throw new BusinessException(ERROR_PARAMS,"不能查询已经过期的队伍信息！");
        }
        //2. 可以通过某一个**关键词**同时对**名称**和**描述**进行查询（sql中写）
        String searchText = request.getSearchText();
        if (searchText!=null&&searchText.length()>512) {
            throw new BusinessException(ERROR_PARAMS,"关键词的字数不能超过512个");
        }
        if (StrUtil.isBlank(searchText)){
            request.setSearchText(null);
        }
        //3. 只有管理员才能查看加密还有非公开的房间
        if (TeamStatusEnum.ENCRYPT.equals(teamStatusEnum)) {
            if (!UserHolder.getUser().getUserRole().equals(ADMINISTRATOR_AUTHORITY)) {
                throw new BusinessException(ERROR_PARAMS,"用户权限不足，只有管理员才能查看加密和非公开的房间");
            }
        }
        //封装sql语句参数
        Integer pageNum = Optional.ofNullable(request.getPageNum()).orElse(1);
        Integer pageSize = Optional.ofNullable(request.getPageSize()).orElse(8);
        TeamListDO teamListDO = new TeamListDO();//创建do
        Map<String,String> map =new HashMap<>();
        map.put("userId","captainId");
        map.put("id","teamId");
        map.put("name","teamName");
        BeanUtil.copyProperties(request,teamListDO, //复制do
                CopyOptions.create().ignoreNullValue()
                        .setFieldMapping(map));
        teamListDO.setOffset((pageNum-1)*pageSize);//设置分页起始下标
        teamListDO.setSize(pageSize);//设置分页大小
        //4. sql查询
        //   1. 关联查询
        //      - 查询加入队伍的所有用户信息
        //      - 仅查询创建房间的房主
        //
        //   2. 非空的条件
        //   3. 不展示已经过期的队伍
        //   4. 不展示已经解散的队伍（isDelete ==1）
        //
        List<TeamListDTO> result = teamMapper.listTeamByCondition2(teamListDO);
        //查询当前用户是否已经加入该队伍
        Long currentUserId = UserHolder.getUser().getId();
        for (TeamListDTO teamListDTO : result) {
            Long teamId = teamListDTO.getTeamId();
            Integer hashJoin = userTeamMapper.isHashJoin(currentUserId,teamId);
            teamListDTO.setHasJoin(hashJoin > 0);
        }
        //5. 返回结果
        return Result.ok(SUCCESS,result);
    }

    @Override
    public Result joinTeam(TeamJoinRequest request) {
        if (BeanUtil.isEmpty(request)) {
            throw new BusinessException(ERROR_PARAMS,"队伍的名称和密码不能为空！");
        }
        String password = request.getPassword();
        Long teamId = request.getTeamId();
        Long userId = UserHolder.getUser().getId();
        long threadId = Thread.currentThread().getId();
        RLock lock = redissonClient.getLock(LOCK_TEAM_JOINTEAM+lockUUid+":"+threadId);
        try {
            if (lock.tryLock(1000,-1, TimeUnit.MILLISECONDS)) {
                //1. 队伍要存在
                Integer count = teamMapper.countTeamByTeamId(teamId);
                if (count==null||count<=0){
                    throw new BusinessException(ERROR_PARAMS,"队伍不存在！");
                }
                //7. 禁止加入私密的队伍
                TeamGetDTO team = teamMapper.getOneTeam(teamId);
                TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(team.getStatus());
                if (TeamStatusEnum.PRIVATE.equals(teamStatusEnum)){
                    throw new BusinessException(ERROR_PARAMS,"不能加入私密的队伍！");
                }
                //2. 队伍人数未满
                //查询队伍的全部信息
                Integer maxNum = team.getMaxNum();
                //查询队伍的加入人数
                Integer countTeamHashJoin = userTeamMapper.countTeamHashJoin(teamId);
                if (maxNum<countTeamHashJoin) {
                    throw new BusinessException(ERROR_PARAMS,"队伍的人数已满！");
                }
                //3. 队伍不能过期
                LocalDateTime expireTime = team.getExpireTime();
                if (expireTime.isBefore(LocalDateTime.now())) {
                    throw new BusinessException(ERROR_PARAMS,"队伍已经过期！");
                }
                //4. 一个用户不能加入超过5个队伍
                Integer countUserJoin = userTeamMapper.countUserJoinTeam(userId);
                if (countUserJoin>=MAX_USER_JOINTEAM){
                    throw new BusinessException(ERROR_PARAMS,"用户不能加入超过5个队伍！");
                }
                //5. 如果队伍的状态是加密的，要使用密码进入

                String correctPassword = team.getPassword();
                if (TeamStatusEnum.ENCRYPT.equals(teamStatusEnum)&&!StrUtil.equals(correctPassword,password)) {
                    throw new BusinessException(ERROR_PARAMS,"密码不正确！");
                }
                //6. 不能加入自己的队伍，不能重复加入已经加入的队伍（幂等性）
                if (team.getCaptainId().equals(userId)) {
                    throw new BusinessException(ERROR_PARAMS,"不能加入自己的队伍！");
                }
                if (userTeamMapper.isHashJoin(userId,teamId)>0) {
                    throw new BusinessException(ERROR_PARAMS,"不能加入已经加入的队伍！");
                }
                //9. 新增 **队伍 - 用户** 关联信息
                UserTeamAddDO userTeamAddDO = new UserTeamAddDO(userId,teamId);
                Integer resultCount = userTeamMapper.addOne(userTeamAddDO);
                if (resultCount<=0){
                    throw new BusinessException(ERROR_SYSTEM,"加入队伍失败！");
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally{
            lock.unlock();
        }
        return Result.ok(SUCCESS);
    }

    @Override
    @Transactional
    public Result quitTeam(TeamQuitRequest request) {
        //1. 校验请求参数
        if (BeanUtil.isEmpty(request)) {
            throw new BusinessException(ERROR_PARAMS,"请选择要退出的队伍！");
        }
        Long teamId = request.getTeamId();
        Long userId = UserHolder.getUser().getId();
        //2. 校验队伍是否存在
        TeamGetDTO team = teamMapper.getOneTeam(teamId);
        if (BeanUtil.isEmpty(team)){
            throw new BusinessException(ERROR_PARAMS,"队伍不存在！");
        }
        //3. 校验我是否已经加入队伍（user-team中是否存在这条数据）
        Integer hashJoin = userTeamMapper.isHashJoin(userId, teamId);
        if (hashJoin == null || hashJoin <= 0){
            throw new BusinessException(ERROR_USER_OPTIONS,"当前没有加入该队伍！");
        }
        //4. 如果队伍只剩一人，解散队伍。
        Integer countTeamHashJoin = userTeamMapper.countTeamHashJoin(teamId);
        if (countTeamHashJoin<=1){//校验到这里，说明当前用户是最有一位用户，且是队长。
            Integer deleteOneTeam = teamMapper.deleteOneTeam(teamId);//将team删除
            if (deleteOneTeam==null||deleteOneTeam<=0) {
                throw new BusinessException(ERROR_SYSTEM,"系统异常！");
            }
            Integer deleteOneTeam1 = userTeamMapper.deleteOneTeam(teamId);//将user_team删除
            if (deleteOneTeam1==null||deleteOneTeam1<=0) {
                throw new BusinessException(ERROR_SYSTEM,"系统异常！");
            }
            return Result.ok(SUCCESS);
        }
        //5. 如果队伍还有其他人
        //   1. 如果是队长退出队伍，权限转移给第二早加入的用户——先来后到（根据加入的user_team的创建时间来判断）
        if (userId.equals(team.getCaptainId())){
            List<UserDTO> userDTOList = userTeamMapper.getAllJoinUser(teamId);
            if (CollectionUtil.isEmpty(userDTOList)||userDTOList.size()<=1) {
                throw new BusinessException(ERROR_SYSTEM,"系统异常！");
            }
            UserDTO nextCaptain = userDTOList.get(1);
            Long nextCaptainId = nextCaptain.getId();
            Integer count = teamMapper.updateCaptainId(nextCaptainId,teamId);//更新队长
            if (count==null||count<=0){
                throw new BusinessException(ERROR_SYSTEM,"队长更新失败！");
            }
        }
        //   2. 非队长，自己退出队伍
        Integer resultCount = userTeamMapper.quitTeam(userId,teamId);//当前用户退出队伍
        if (resultCount==null||resultCount<=0){
            throw new BusinessException(ERROR_SYSTEM,"退出队伍失败！");
        }
        return Result.ok(SUCCESS,resultCount);
    }

    @Override
    public Result listMyJoinTeamByPage(TeamListRequest request) {
        //核心参数为userId，其余的参数可以为空。不需要校验参数
        //1. TeamListDO数据持久层交互对象加上一个队伍列表teamIdList
        //2. 查询出user_team中包含userId所有的数据
        Long userId = UserHolder.getUser().getId();
        //查看当前用户是否有加入到任意队伍
        Integer count = userTeamMapper.countUserJoinTeam(userId);
        if (count==null||count<=0){
            throw new BusinessException(ERROR_PARAMS,"当前没有加入任何队伍！");
        }
        List<UserTeamGetDTO> userTeamGetDTOList = userTeamMapper.getAllJoinTeam(userId);
        //3. 使用流进行过滤处理，分组(去重，防止出错，进一步保险)
        Map<Long, List<UserTeamGetDTO>> map = userTeamGetDTOList.stream().collect(Collectors.groupingBy(UserTeamGetDTO::getTeamId));
        Set<Long> teamIdSet = map.keySet();
        //4. 将teamId分装进teamIdList
        TeamListDO teamListDO = new TeamListDO();
        teamListDO.setTeamIdSet(teamIdSet);
            //设置分页参数
        Integer pageNum = Optional.ofNullable(request.getPageNum()).orElse(1);
        Integer pageSize = Optional.ofNullable(request.getPageSize()).orElse(8);
        teamListDO.setOffset((pageNum-1)*pageSize);
        teamListDO.setSize(pageSize);
            //将条件封装
        BeanUtil.copyProperties(request,teamListDO,CopyOptions.create().setFieldMapping(MapUtil.of("name","teamName")));
        //5. 调用原来的接口，进行条件查询
        List<TeamListDTO> result = teamMapper.listTeamByCondition2(teamListDO);
        if (CollectionUtil.isEmpty(result)){
            throw new BusinessException(ERROR_SYSTEM,"系统出现异常！");
        }
        //6. 返回结果
        return Result.ok(SUCCESS,result);
    }

    @Override
    public Result listMyCreatedTeam(TeamListRequest request) {
        //核心参数为userId，其余的参数可以为空。不需要校验参数
        //1. 往TeamListDO中添加当前用户的id
        TeamListDO teamListDO = new TeamListDO();
        Long userId = UserHolder.getUser().getId();
        teamListDO.setCaptainId(userId);
        //封装参数
        BeanUtil.copyProperties(request,teamListDO,CopyOptions.create().setFieldMapping(MapUtil.of("name","teamName")));
        Integer pageNum = Optional.ofNullable(request.getPageNum()).orElse(1);
        Integer pageSize = Optional.ofNullable(request.getPageSize()).orElse(8);
        teamListDO.setOffset((pageNum-1)*pageSize);
        teamListDO.setSize(pageSize);
        //2. 调用方法，查询结果
        List<TeamListDTO> teamListDTOS = teamMapper.listTeamByCondition(teamListDO);
        if (CollectionUtil.isEmpty(teamListDTOS)) {
            throw new BusinessException(ERROR_RESULT,"结果为空！");
        }
        //3. 返回结果
        return Result.ok(SUCCESS,teamListDTOS);
    }


    /**
     * 校验添加队伍的参数
     * @param request addTeam请求的请求封装对象
     */
    private  void validAddTeamParams(TeamAddRequest request) {
        //   1- 队伍人数：null、1<= maxNum <=20
        Integer maxNum = request.getMaxNum();
        if (maxNum!=null&&(maxNum<1||maxNum>20)){
            throw new BusinessException(ERROR_PARAMS,"队伍人数的范围为1~20");
        }
        //   2- 队伍标题：null、2<= length <=20
        String teamName = request.getName();
        if (!StrUtil.isBlank(teamName)&&(teamName.length()<2||teamName.length()>20)){
            throw new BusinessException(ERROR_PARAMS,"队伍的标题字数的范围为2~20");
        }
        //   3- 描述：0<= length <= 512
        String description = request.getDescription();
        if (!StrUtil.isBlank(description)&&description.length()>512){
            throw new BusinessException(ERROR_PARAMS,"队伍的描述字数范围为0~512");
        }
        //   4- status：类型为int  0 - 公开 1 - 私密 2 - 加密
        Integer status = Optional.ofNullable(request.getStatus()).orElse(0);//判断非空，然后赋默认值
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);//根据值获取枚举
        if (teamStatusEnum==null) {
            throw new BusinessException(ERROR_PARAMS,"队伍状态错误！");
        }
        //   5- 如果是加密状态，要有密码，密码：  4 <= length <= 12
        String password = request.getPassword();
        if (TeamStatusEnum.ENCRYPT.equals(teamStatusEnum)){
            if (StrUtil.isBlank(password)||password.length()<4||password.length()>12){
                throw new BusinessException(ERROR_PARAMS,"队伍加密状态下的密码长度范围为4~12");
            }
        }
        //   6- 超时时间：> now()
        LocalDateTime expireTime = request.getExpireTime();
        if (expireTime!=null&&expireTime.isBefore(LocalDateTime.now())){
            throw new BusinessException(ERROR_PARAMS,"队伍的有效时间错误！");
        }
        //   7- 用户最多创建5个队伍
        Long userId = UserHolder.getUser().getId();
        Integer totalTeam = teamMapper.countTeamByUserId(userId);
        if (totalTeam>=5){
            throw new BusinessException(ERROR_PARAMS,"用户最多只能创建5个队伍！");
        }
    }
}
