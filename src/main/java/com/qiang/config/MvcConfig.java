package com.qiang.config;

import com.qiang.interceptor.LoginInterceptor;
import com.qiang.interceptor.RefreshTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //拦截除了登录注册以外的所有请求
        registry.addInterceptor(new LoginInterceptor())
                .excludePathPatterns(
                        "/user/regist",
                        "/user/login",
                        "/user/sendCode"
                ).order(1);
        //拦截所有的请求，获取到token
        registry.addInterceptor(new RefreshTokenInterceptor(stringRedisTemplate))
                .addPathPatterns("/**").order(0);
    }
}
