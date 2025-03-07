package com.qiang.service;

import com.qiang.domain.entity.Result;
import com.qiang.domain.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;

import java.net.http.HttpRequest;

@Transactional
public interface UserService {
    Result addOneUser(User user);

    Result getOneUser(Integer id);

    Result register(String userAccount, String userPassword,String checkCode);

    Result sendCode(String userAccount);

    Result login(String userAccount, String userPassword);

    Result deleteOneUser(Integer id);

    Result logout(HttpServletRequest request);
}
