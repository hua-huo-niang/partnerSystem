package com.qiang.interceptor;

import com.qiang.comment.exception.BusinessException;
import com.qiang.comment.ErrorCode;
import com.qiang.util.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录校验拦截器
 *  作用：验证当前用户是否已经登录，如果已经登录，当前请求线程中的UserHolder不为空
 * 拦截效果：
 *  没登陆的用户将会返回错误信息
 *  已经登录的用户会被放行
 */

public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("正在进入到登录校验拦截器中》》》》》》》》》》》》");
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())){
            response.setStatus(HttpServletResponse.SC_OK);
            return false;//直接返回不执行后续逻辑
        }
        //没有用户，设置状态码，返回false
        if (UserHolder.getUser()==null) {
            response.setStatus(401);
            throw  new BusinessException(ErrorCode.ERROR_STATUS,"状态异常，未登录");
        }
        //有用户放行
        return true;
    }
}
