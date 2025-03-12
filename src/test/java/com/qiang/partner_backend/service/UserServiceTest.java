package com.qiang.partner_backend.service;

import com.qiang.domain.entity.Result;
import com.qiang.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class UserServiceTest{

    @Autowired
    private UserService userService;

    @Test
    void testSearchUserByTagNameList(){
        List<String> str = Arrays.asList("java","python");
        Result result = userService.getUsersByTagName(str);
        System.out.println(result);

    }
}
