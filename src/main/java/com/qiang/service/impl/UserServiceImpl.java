package com.qiang.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.*;
import cn.hutool.crypto.digest.BCrypt;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qiang.domain.DTO.UserDTO;
import com.qiang.domain.entity.Result;
import com.qiang.domain.entity.User;
import com.qiang.domain.entity.UserHolderEntity;
import com.qiang.exception.BusinessException;
import com.qiang.mapper.UserMapper;
import com.qiang.service.UserService;
import com.qiang.util.LuaUtil;
import com.qiang.util.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import static com.qiang.constant.UserConstant.*;
import static com.qiang.util.ErrorCode.*;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    private Gson gson = new Gson();

    @Override
    public Result addOneUser(User user) {
        Integer count  = userMapper.addOne(user);
        return Result.ok(SUCCESS,count);
    }

    /**
     * 根据用户id取获取一个用户
     * @param id 用户id
     * @return Result 统一响应对象
     */
    @Override
    public Result getOneUser(Long id) {
         UserDTO userDTO = userMapper.getOne(id);
         if (BeanUtil.isEmpty(userDTO)){
             throw new BusinessException(ERROR_USER_OPTIONS,"该用户不存在！");
         }
        System.out.println(userDTO);
        return Result.ok(SUCCESS,userDTO);//返回DTO对象
    }


    @Override
    public Result deleteOneUser(Integer id) {
        //1.获取当前登录用户的权限
        UserHolderEntity currentUser = UserHolder.getUser();
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

    /**
     * 获取请求头中的token，从redis中获取用户信息
     * @return  Result 统一响应对象
     */
    @Override
    public Result getCurrentUser(String token) {
        //1. 获取request中的authroization中的token
        //2. 拼接key
        String key = LOGIN_TOKEN+token;
        //3. 从redis中查询用户信息
        Map<Object, Object> userMap =  stringRedisTemplate.opsForHash().entries(key);
        //4. 判断非空，如果为空，查询数据库
        if (MapUtil.isEmpty(userMap)){
            UserDTO userDTO = userMapper.getOne(UserHolder.getUser().getId());
            Map<String, Object> resultMap = BeanUtil.beanToMap(userDTO,
                    new HashMap<>(),
                    CopyOptions.create().
                            setIgnoreNullValue(true).
                            setFieldValueEditor((fieldName, fieldValue) -> fieldValue == null ? null : fieldValue.toString()));
            //5. 将从数据库中查询到的数据存放到redis中
            stringRedisTemplate.opsForHash().putAll(key,resultMap);
        }
        //6. 如果不为空，直接返回redis中的用户信息
        return Result.ok(SUCCESS,userMap);
    }
    @Override
    public Result updateUser(UserDTO data, String token) {
        String key = LOGIN_TOKEN+token;
        //校验用户权限，看看是不是当前用户。用当前登录的信息和要修改的信息进行比较
        if (!UserHolder.getUser().getId().equals(data.getId())) {
            throw new BusinessException(ERROR_PARAMS,"只能修改自己的信息！");
        }
        List<String> args = new ArrayList<>();
        //1.数据库修改数据
        Integer dbCount = userMapper.updateOneUser(data);
        //2.如果修改失败则返回错误
        if (dbCount<=0){
            throw new BusinessException(ERROR_SYSTEM,"系统异常，更新失败");
        }
        //3.修改redis中数据，这里使用lua脚本执行
        //TODO 可以考虑使用redission来优化redis中的数据删除，也就是加锁保证线程安全
        Arrays.stream(ReflectUtil.getFields(UserDTO.class)).forEach(field -> {
            Object value = ReflectUtil.getFieldValue(data, field);
            if (ObjectUtil.isNotEmpty(value)){
                args.add(field.getName());//将key放入
                args.add(value.toString());//将value放入
            }
        });
        //执行lua脚本语句 更新redis中的数据
        LuaUtil.updateUser(stringRedisTemplate, Collections.singletonList(key), args);
        return Result.ok(SUCCESS,dbCount);
    }



    /**
     * 根据标签列表去查询用户。如果用户带有这个标签，则返回这个用户。
     * @param tagNameList 查询的标签列表
     * @return Resut 统一响应对象
     */
    @Override
    public Result getUsersByTagName(List<String> tagNameList) {
        /*
        * 1.方法一：sql查询，like %% and like %% (存储的是json格式的字符串，而不是json)
        * 2.方法二：内存查询。先用sql查询出大概的，再在内存中过过滤
        * 3.方法三：sql大致查询出含有部分的，内存种再仔细过滤
        * 4.方法四：当数据库连接足够，空间足够的时候，使用并发查询
        * 5.方法五：当数据库中存储的是json，可以使用json特有的查询方式，配合上内存过滤*/

        //方法一
//        return getUsersByTagName_SQL(tagNameList);


        //方法二
//        return getUsersByTagName_memery(tagNameList);

        //方法三
//        return getUsersByTagName_sql_memery(tagNameList);

        //方法四
//        return getUsersByTagName_conCurrent(tagNameList);

        //方法五
        return getUsersByTagName_json(tagNameList);
    }



    /**
     * 根据标签列表去查询带有全部标签的用户
     * 如果数据库中字段数据格式是json，可以使用json特有的查询方式
     * 速度较快，查找结果精确
     * @param tagNameList
     * @return
     */
    private Result getUsersByTagName_json(List<String> tagNameList) {
        //大致查询数据库
        List<UserDTO> userDTOS = userMapper.getuserByAnyTagname_josn(tagNameList);
        //判断非空
        if (userDTOS.isEmpty()) {
            throw new BusinessException(ERROR_RESULT,"不存在这样的用户！");
        }
        return Result.ok(SUCCESS,userDTOS);
    }

    /**
     * 根据标签列表查询包含所有标签的用户。
     * 当数据库连接足够，空间足够的情况下，可以使用并发查询，谁返回先，就使用谁
     * 用completionService.take().get()会阻塞直到第一个任务返回，谁快用谁
     * @param tagNameList
     * @return
     */
    private Result getUsersByTagName_conCurrent(List<String> tagNameList) {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        ExecutorCompletionService<Object> completionService = new ExecutorCompletionService<>(executorService);
        //提交sql查询任务
        completionService.submit(()->getUsersByTagName_SQL(tagNameList));
        completionService.submit(()->getUsersByTagName_memery(tagNameList));
        Result result = null;
        try{
            result = (Result) completionService.take().get();
        }catch (Exception e){
            throw new BusinessException(ERROR_OTHER,"出错了！");
        }finally {
            executorService.shutdown();
        }
        if (result==null){
            throw new BusinessException(ERROR_OTHER,"出错了！");
        }
        return result;
    }

    /**
     * 根据标签列表查询包含所有标签的用户。
     * sql大致查询，查询出包含任意标签的用户。内存中再仔细过滤。
     * @param tagNameList
     * @return
     */
    private Result getUsersByTagName_sql_memery(List<String> tagNameList) {
        //在sql中大致查询
        List<UserDTO> userDTOS = userMapper.getUserByAnyTagName(tagNameList);
        //判断非空  可以使用optional来进行处理
        /*if (userDTOS.isEmpty()){
            throw new BusinessException(ERROR_RESULT,"不存在这样的用户");
        }*/
        //在内存中过滤
        Gson gson = new Gson();
        List<UserDTO> userList = userDTOS.stream().filter(userDTO -> {
            String tagsStr = userDTO.getTags();
            if (tagsStr.isEmpty()){
                return false;
            }
            Set<String> tagSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
            }.getType());
            tagSet = Optional.ofNullable(tagSet).orElse(new HashSet<>());//封装可能为空的对象，如果为空就用原本的值，如果为空，就使用传入的值。减少分支，圈复杂度，消除没有意义的分支
            return tagSet.containsAll(tagNameList);
        }).collect(Collectors.toList());
        //判断非空
        if (userList.isEmpty()) {
            throw new BusinessException(ERROR_RESULT,"不存在这样的用户");
        }
        //返回
        return Result.ok(SUCCESS,userList);
    }

    /**
     * 根据标签列表查询包含所有标签的用户。
     * 使用纯内存过滤的方式。
     * 查询全部的用户，在内存中仔细过滤
     * @param tagNameList
     * @return
     */
    private Result getUsersByTagName_memery(List<String> tagNameList) {
        //查询出所有的用户
        List<UserDTO> userList = userMapper.getAllUsers();
        if (userList.isEmpty()){
            throw new BusinessException(ERROR_RESULT,"不存在这样的用户");
        }
        //遍历用户
        Gson gson = new Gson();
        List<UserDTO> userResult = userList.stream().filter(userDTO -> {
            //从用户中获取tag json字符串 ["java","js","C++"]
            String tagsStr = userDTO.getTags();
            if (tagsStr==null||tagsStr.isEmpty()){
                return false;
            }
            //使用GSON将字符串转为set集合，set集合判断是否包含速度快
            Set<String> tagSet = gson.fromJson(userDTO.getTags(), new TypeToken<Set<String>>() {
            }.getType());
            System.out.println("该用户的标签是："+tagsStr);
            for (String tagName : tagNameList) {
                //看看是否包含目标字符串
                if (!tagSet.contains(tagName)) {
                    //不包含就直接返回错误
                    return false;
                }
            }
            //包含就保留
            return true;//到这，说明都包含了，返回true，过滤中保留
        }).collect(Collectors.toList());
        //过滤之后进行判断
        if (userResult.isEmpty()) {
            throw new BusinessException(ERROR_RESULT,"不存在这样的用户!");
        }
        return Result.ok(SUCCESS, userResult);
    }

    /**
     * 根据标签列表查询包含所有标签的用户。
     * 纯sql查询方法。like %% and like %%
     * 无法实现精确查找，只能模糊。找java,会返回javaScript
     * @param tagNameList
     * @return
     */
    private Result getUsersByTagName_SQL(List<String> tagNameList) {
        List<UserDTO> userList = userMapper.getUserByTagNameList(tagNameList);
        if (userList.isEmpty()){
            throw new BusinessException(ERROR_RESULT,"不存在这样的用户");
        }
        return Result.ok(SUCCESS, userList);
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
        return Result.ok(SUCCESS,userDTO,token);
    }


}
