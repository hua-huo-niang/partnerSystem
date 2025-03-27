package com.qiang.service;

import com.qiang.domain.DTO.user.UserDTO;
import com.qiang.comment.Result;
import com.qiang.domain.BO.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface UserService {
    Result addOneUser(User user);

    Result register(String userAccount, String userPassword,String checkCode);

    Result sendCode(String userAccount);

    Result login(String userAccount, String userPassword);

    Result getOneUser(Long id);

    Result deleteOneUser(Integer id);

    Result logout(HttpServletRequest request);

    Result getUsersByTagName(List<String> tagNameList);

    Result getCurrentUser(String token);

    Result updateUser(UserDTO data, String token);

    Result recommendUsersByPage(Integer pageNum, Integer pageSize);
}
