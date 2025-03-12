package com.qiang.interceptor;

import com.qiang.exception.BusinessException;
import com.qiang.util.ErrorCode;
import com.qiang.util.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

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
