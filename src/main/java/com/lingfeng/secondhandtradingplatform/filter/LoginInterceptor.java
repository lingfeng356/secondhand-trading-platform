package com.lingfeng.secondhandtradingplatform.filter;


import com.lingfeng.secondhandtradingplatform.util.UserUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取token
        String token = request.getHeader("token");
        Long userId = UserUtils.getIdByToken(token);

        //校验token
        //为null，拦截
        if(userId == null){
            response.setStatus(401);
            return false;
        }

        //放行
        return true;
    }
}
