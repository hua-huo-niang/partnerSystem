package com.qiang.config;

import com.qiang.interceptor.LoginInterceptor;
import com.qiang.interceptor.RefreshTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
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
                        "/user/regist/**",
                        "/user/login/**",
                        "/user/sendCode/**",
                        "/static/**",
                        "/webjars/**",
                        "/favicon.ico",
                        "/favicon.ico/**",
                        "/swagger-resources/**",
                        "/v2/api-docs/**",
                        "/v3/api-docs/**",
                        "/v3/api-docs/swagger-config/**",
                        "/doc.html",
                        "/swagger-ui/**",
                        "/swagger-ui.html"
                ).order(1);
        //拦截所有的请求，获取到token
        registry.addInterceptor(new RefreshTokenInterceptor(stringRedisTemplate))
                .addPathPatterns("/**").order(0);
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/swagger-ui/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");

        registry.addResourceHandler("/doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
    }

}
