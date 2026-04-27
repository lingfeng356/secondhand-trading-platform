package com.lingfeng.secondhandtradingplatform.filter;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

import static com.lingfeng.secondhandtradingplatform.constant.RedisConstant.LOGIN_TOKEN_KEY;
import static com.lingfeng.secondhandtradingplatform.constant.RedisConstant.LOGIN_USER_TTL;

@Slf4j
@Component
public class RefreshInterceptor implements HandlerInterceptor {

    private StringRedisTemplate stringRedisTemplate;

    public RefreshInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取token
        String token = request.getHeader("token");

        //判断token是否存在
        if(token != null && !token.isEmpty()){
            //判断redis中tokenKey是否存在
            String tokenKey = LOGIN_TOKEN_KEY + token;
            boolean flag = stringRedisTemplate.hasKey(tokenKey);

            if(Boolean.TRUE.equals(flag)){
                //刷新TTL
                stringRedisTemplate.expire(tokenKey,LOGIN_USER_TTL, TimeUnit.MINUTES);
                log.info("已刷新token持续时间");
            }
        }

        //放行
        return true;
    }
}
