package com.qiang.interceptor;

import cn.hutool.core.bean.BeanUtil;
import com.qiang.domain.Holder.UserHolderEntity;
import com.qiang.util.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.qiang.constant.UserConstant.LOGIN_TOKEN;
import static com.qiang.constant.UserConstant.LOGIN_TOKEN_TTL;


/**
 * 登录token有效期刷新拦截器
 * 主要作用：
 *  拦截当前这个用户的所有请求，并将redis中的token有效期进行刷新
 * 次要作用：
 *  封装UserHolder，在当前业务请求中标识用户，知道是哪一个用户在使用业务
 * 拦截效果：
 *  全部放行，登录校验在LoginInterceptor中进行
 *  在请求响应的时候，将UserHolder进行删除。因为Tomcat服务器的线程是使用的线程池中的，不删除会对其他线程造成影响
 */
public class RefreshTokenInterceptor implements HandlerInterceptor {
    private StringRedisTemplate stringRedisTemplate;

    //因为拦截器还没有进入到spring中，所以没能使用spring容器的自动注入，要使用构造方法，在config中传入
    public RefreshTokenInterceptor(StringRedisTemplate redisTemplate){
        this.stringRedisTemplate = redisTemplate;
    }
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取完整请求 URL，包括路径和查询参数
        String url = request.getRequestURL().toString();
        String queryString = request.getQueryString();
        if (queryString != null) {
            url += "?" + queryString;
        }

        // 打印请求 URL
        System.out.println("拦截的请求路径: " + url);
        //1.获取请求头中的token
        String token = request.getHeader("authorization");
        //2.根据token获取redis中的信息
        String key = LOGIN_TOKEN+token;
        Map<Object, Object> map = stringRedisTemplate.opsForHash().entries(key);
        //3.用户为空，放行return true
        if (map.isEmpty()){
            return true;
        }
        //4.将查询到的数据封装为UserHolderEntity对象
        UserHolderEntity userHolderEntity = BeanUtil.fillBeanWithMap(map, new UserHolderEntity(), false);
        //5.将数据保存到ThreadLocal中
        UserHolder.saveUser(userHolderEntity);
        System.out.println("当前登录的用户是："+userHolderEntity);
        //6.刷新token有效期
        stringRedisTemplate.expire(key,LOGIN_TOKEN_TTL, TimeUnit.MINUTES);
        //7.放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();//回去的时候删除UserHolder，因为线程是从线程池中获取的。不删除可能会对其他线程使用有影响
    }
}
