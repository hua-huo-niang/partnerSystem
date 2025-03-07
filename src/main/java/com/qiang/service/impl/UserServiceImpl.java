package com.qiang.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.qiang.domain.DTO.UserDTO;
import com.qiang.domain.entity.Result;
import com.qiang.domain.entity.User;
import com.qiang.exception.BusinessException;
import com.qiang.mapper.UserMapper;
import com.qiang.service.UserService;
import com.qiang.util.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.qiang.constant.UserConstant.*;
import static com.qiang.util.ErrorCode.*;


@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result addOneUser(User user) {
        Integer count  = userMapper.addOne(user);
        return Result.ok(SUCCESS,count);
    }

    /**
     *
     * @param id
     * @return
     */
    @Override
    public Result getOneUser(Integer id) {
         User user = userMapper.getOne(id);
         if (BeanUtil.isEmpty(user)){
             throw new BusinessException(ERROR_USER_OPTIONS,"该用户不存在！");
         }
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        System.out.println(userDTO);
        return Result.ok(SUCCESS,userDTO);//返回DTO对象
    }


    @Override
    public Result deleteOneUser(Integer id) {
        //1.获取当前登录用户的权限
        UserDTO currentUser = UserHolder.getUser();
        Integer userRole = currentUser.getUserRole();
        if (userRole!=ADMINISTRATOR_AUTHORITY){
            throw new BusinessException(ERROR_AUTH,"用户权限不足");
        }
        Integer count = userMapper.deleteOne(id);
        if (count == 0){
            throw new BusinessException(ERROR_USER_OPTIONS,"该用户不存在，删除失败！");
        }
        return Result.ok(SUCCESS,count);
    }

    @Override
    public Result logout(HttpServletRequest request) {
        //获取token
        String token = request.getHeader("Authorization");
        if (StrUtil.isBlank(token)){
            Result.ok(SUCCESS,"当前状态为：未登录！");
        }
        String key = LOGIN_TOKEN+token;
        stringRedisTemplate.delete(key);

        return Result.ok(SUCCESS,"退出成功！");
    }


    @Override
    public Result register(String userAccount, String userPassword,String checkCode) {
        //账号判断
        Result failAccount = validAccount(userAccount);
        if (failAccount != null) return failAccount;
        //密码判断
        Result failPassword = validPassword(userPassword);
        if (failPassword != null) return failPassword;
        //验证码判断
        if (StrUtil.isBlank(checkCode)){
            return Result.fail(ERROR_PARAMS,"验证码不能为空！");
        }
        // TODO 如果要保证原子性，这里可以改进为使用lua脚本。防止多个线程同时获取验证码进行注册，只允许一个线程，保证线程安全。
        // 连接redis，拿到redis中存储的验证码进行校验。
        String key = REGIST_CODE +userAccount;
        String redisCode = stringRedisTemplate.opsForValue().get(key);
        //如果使用的账号不是 生成验证码的账号，就报错
        if (StrUtil.isBlank(redisCode)){
            return Result.fail(ERROR_PARAMS,"账号和验证码不匹配！或验证码只能使用一次！");
        }

        if (!redisCode.equals(checkCode)){
            return Result.fail(ERROR_PARAMS,"验证码错误！");
        }
        //到这，说明验证码正确，删除验证码，只能使用一次。
        stringRedisTemplate.delete(key);
        //查询数据库中是否已经存在用户
        Integer count = userMapper.findOnRegist(userAccount);
        if (count>0){
            return Result.fail(ERROR_USER,"用户已经存在!");
        }
        //将用户的密码进行加密后保存到数据库  使用BCrypt
        String hashPassword = BCrypt.hashpw(userPassword, BCrypt.gensalt());
        count = userMapper.addOnRegist(userAccount,hashPassword);

        //封装用户信息，返回，用来保存用户，并供以后的请求验证校验是否登录
        if (count>0){
            Long userId = userMapper.getIdByUserAccount(userAccount);
            UserDTO userDTO = new UserDTO();
            userDTO.setId(userId);
            userDTO.setUserAccount(userAccount);
            return Result.ok(SUCCESS,userDTO);
        }
        return Result.fail(null);
    }

    /**
     *
     * @param userPassword 用户密码
     * @return new Result
     */
    private static Result validPassword(String userPassword) {
        if (StrUtil.isBlank(userPassword)){
            return Result.fail(ERROR_PARAMS,"密码不能为空！");
        }
        if (userPassword.length()<8|| userPassword.length()>15){
            return Result.fail(ERROR_PARAMS,"密码长度在8到15位之间！");
        }
        return null;
    }

    /**
     *
     * @param userAccount 用户账号
     * @return new Result
     */
    private static Result validAccount(String userAccount) {
        if (StrUtil.isBlank(userAccount)){
            return Result.fail(ERROR_PARAMS,"账号不能为空！");
        }
        if (userAccount.length()<4|| userAccount.length()>12){
            return Result.fail(ERROR_PARAMS,"账号长度在4到12位之间！");
        }
        if (!ReUtil.isMatch("^[0-9A-Za-z]+$", userAccount)){
            return Result.fail(ERROR_PARAMS,"账号不能包含特殊字符");
        }
        return null;
    }

    @Override
    public Result sendCode(String userAccount) {
        //校验账号
        Result failAccount = validAccount(userAccount);
        if (failAccount!=null){return failAccount;}

        //使用计数器记录同一个账号获取验证码的次数，防止恶意多次注册
        String captcha_count_key = CAPTCHA_COUNT+userAccount;
        Long count = stringRedisTemplate.opsForValue().increment(captcha_count_key, 1);
        if (count==1){//第一次申请
            stringRedisTemplate.expire(captcha_count_key,CAPTCHA_COUNT_TTL,TimeUnit.MINUTES);
        }
        if (count>3){
            return Result.fail(ERROR_USER_OPTIONS,"验证码请求过于频繁，请稍后重试！");
        }
        // 判断账号是否存在
        Long userId = userMapper.getIdByUserAccount(userAccount);
        if (userId!=null){
            return Result.fail(ERROR_USER,"账号已经存在！") ;
        }
        //随机生成验证码
        int[] ints = NumberUtil.generateRandomNumber(100000, 999999, 1);
        String code = String.valueOf(ints[0]);
        //拼接key
        String key = REGIST_CODE +userAccount;
        //将验证码保存到redis缓存中
        stringRedisTemplate.opsForValue().set(key,code,REGIST_CODE_TTL,TimeUnit.MINUTES);
        System.out.println(code);
        return Result.ok(SUCCESS);
    }

    @Override
    public Result login(String userAccount, String userPassword) {
        //校验账户和密码
        Result validAccount = validAccount(userAccount);
        if (validAccount!=null){
            return validAccount;
        }
        Result validPassword = validPassword(userPassword);
        if (validPassword!=null){
            return validPassword;
        }
        String keyFail = LOGIN_FAIL+userAccount;
        String failCountStr =stringRedisTemplate.opsForValue().get(keyFail);
        if (!StrUtil.isBlank(failCountStr)){
            long failCount = Long.parseLong(failCountStr);
            if (failCount>=ALLOWABLE_ERROR_COUNT){
                return Result.fail(ERROR_USER_OPTIONS,"密码错误10次以上，请稍后重试！");
            }
        }

        //根据账户select用户的密码
        User user = userMapper.getUserByAccount(userAccount);
        if (BeanUtil.isEmpty(user)){
            throw new BusinessException(ERROR_PARAMS,"账号不存在");
        }
        String hashPassword = user.getUserPassword();
        //将当前的密码进行加密，然后与数据库中的进行比较
        if (!BCrypt.checkpw(userPassword,hashPassword)) {
            //如果不同，就会错误
             Long failCount = stringRedisTemplate.opsForValue().increment(keyFail, 1);
            if (failCount>=ALLOWABLE_ERROR_COUNT){
                stringRedisTemplate.expire(keyFail,LOGIN_FAIL_TTL,TimeUnit.MINUTES);
            }
            return Result.fail(ERROR_USER_OPTIONS,"密码错误，您还可以重试："+(ALLOWABLE_ERROR_COUNT-failCount)+"次");
        }
        //如果相同，删除错误尝试计数器，生成token用于登录校验，并返回
        stringRedisTemplate.delete(keyFail);
        String token = IdUtil.fastUUID();
        String key = LOGIN_TOKEN+token;
        //封装成userDTO
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        //将Long类型的id转为String，不然StringRedisTemplate会报错，无法封装
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue==null?null:fieldValue.toString()));
        stringRedisTemplate.opsForHash().putAll(key,userMap);
        stringRedisTemplate.expire(key,LOGIN_TOKEN_TTL,TimeUnit.MINUTES);
        return Result.ok(SUCCESS,token);
    }


}
