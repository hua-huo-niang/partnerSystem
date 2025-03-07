package com.qiang.controller;

import com.qiang.domain.entity.Result;
import com.qiang.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpRequest;

@RestController
@RequestMapping("/user")
public class UserController {


    @Autowired
    private UserService userService;



    @PostMapping("/regist")
    public Result getOneUser(@RequestParam("userAccount")String userAccount,
                             @RequestParam("userPassword") String userPassword,
                             @RequestParam("checkCode") String checkCode){
        return userService.register(userAccount,userPassword,checkCode);
    }


    @PostMapping("/sendCode")
    public Result sendCode(@RequestParam("userAccount") String userAccount){
        return userService.sendCode(userAccount);
    }

    @PostMapping("/login")
    public Result login(@RequestParam("userAccount") String userAccount,
                        @RequestParam("userPassword")String userPassword){
        return userService.login(userAccount,userPassword);

    }


    @GetMapping("/{id}")
    public Result getOneUser(@PathVariable("id") Integer id){
        return userService.getOneUser(id);
    }

    @DeleteMapping("/{id}")
    public Result deleteOneUser(@PathVariable("id") Integer id){return userService.deleteOneUser(id);}


    @PostMapping("/logout")
    public Result logout(HttpServletRequest request){
        return userService.logout(request);
    }
}
