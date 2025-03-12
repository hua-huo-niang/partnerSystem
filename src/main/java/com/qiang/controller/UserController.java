package com.qiang.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.qiang.domain.DTO.UserDTO;
import com.qiang.domain.entity.Result;
import com.qiang.exception.BusinessException;
import com.qiang.service.UserService;
import com.qiang.util.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpRequest;
import java.util.List;
import java.util.Map;

import static com.qiang.util.ErrorCode.ERROR_PARAMS;
import static com.qiang.util.ErrorCode.ERROR_STATUS;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:3000"})
public class UserController {


    @Autowired
    private UserService userService;


    /**
     * 根据用户账户，用户密码和校验码来注册一个用户
     * @param userAccount  用户账号
     * @param userPassword  用户密码
     * @param checkCode  校验码
     * @return Result 统一响应对象
     */
    @PostMapping("/regist")
    public Result getOneUser(@RequestParam("userAccount")String userAccount,
                             @RequestParam("userPassword") String userPassword,
                             @RequestParam("checkCode") String checkCode){
        return userService.register(userAccount,userPassword,checkCode);
    }


    /**
     * 使用用户账号发送一个验证码
     * @param userAccount  用户账号
     * @return Result 统一响应对象
     */
    @PostMapping("/sendCode")
    public Result sendCode(@RequestParam("userAccount") String userAccount){
        return userService.sendCode(userAccount);
    }

    /**
     * 使用用户账号和密码进行登录。
     * 会生成token。
     * @param requestData  map集合，封装userAccount和userPassword
     * @return Result 统一响应对象
     */
    @PostMapping("/login")
    public Result login(@RequestBody Map<String,String> requestData){
        String userAccount = requestData.get("userAccount");
        String userPassword = requestData.get("userPassword");

        return userService.login(userAccount,userPassword);

    }


    /**
     * 获取当前用户的信息
     * @param null
     * @return Result 统一响应对象
     */
    @GetMapping("/current")
    public Result getCurrentUser(HttpServletRequest request){
        String token = request.getHeader("Authorization");
        if (StrUtil.isBlank(token)){
            throw new BusinessException(ERROR_STATUS,"当前用户未登录！");
        }
        return userService.getCurrentUser(token);
    }

    /**
     * 根据用户id取删除用户
     * @param id 用户id
     * @return Result 统一响应对象
     */
    @DeleteMapping("/{id}")
    public Result deleteOneUser(@PathVariable("id") Integer id){return userService.deleteOneUser(id);}


    /**
     * 根据传入的参数进行更新用户的数据
     * @param data  UserDTO 脱敏的用户数据
     * @return  Result 统一响应对象
     */
    @PutMapping("/update")
    public Result updateUser(@RequestBody UserDTO data){
        System.out.println(data);
        if (BeanUtil.isEmpty(data)) {
            throw new BusinessException(ERROR_PARAMS,"没有要修改的信息！");
        }
        return userService.updateUser(data);
    }

    /**
    * @Description: 退出当前用户
    * @Param: request http请求
    * @return: Result 统一响应对象
    */
    @PostMapping("/logout")
    public Result logout(HttpServletRequest request){
        return userService.logout(request);
    }


    /**
     * 根据传入的标签列表查询包含所有标签的用户
     * @param tagNameList 标签列表 String类型
     * @return Reuslt 统一响应对象
     */
    @ApiOperationSupport(author = "qiang")
    @GetMapping("/search/tags")
    @CrossOrigin(origins = "http://localhost:3000",  allowedHeaders = "*", allowCredentials = "true")
    public Result searchUsersByTags(@RequestParam(required = false)List<String> tagNameList){
        System.out.println("come in ");
        if (tagNameList.isEmpty()) {
            throw new BusinessException(ERROR_PARAMS,"查询的标签不能为空！");
        }
        return userService.getUsersByTagName(tagNameList);
    }




}
