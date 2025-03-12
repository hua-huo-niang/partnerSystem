package com.qiang.partner_backend;

import cn.hutool.core.util.StrUtil;
import com.qiang.domain.entity.Result;
import com.qiang.service.UserService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
class PartnerBackendApplicationTests {
    @Resource
    private UserService userService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Test
    void contextLoads() {


    }

    @Test
    void RegistTest(){
        String userAccount = "43";
        String userPassword = "333";
        String checkCode = "fjkjf1";
        Result result = userService.register(userAccount, userPassword, checkCode);
        Assertions.assertEquals(2000,result.getCode());
        System.out.println(result.toString());
    }

    @Test
    void testHutool(){
        System.out.println(StrUtil.isBlank(""));
    }

    @Test
    void testRedis(){
        redisTemplate.opsForValue().set("test","test");
        String result = redisTemplate.opsForValue().get("test");
        System.out.println(result);
    }

}
