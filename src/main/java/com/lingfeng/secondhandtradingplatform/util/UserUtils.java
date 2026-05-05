package com.lingfeng.secondhandtradingplatform.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import static com.lingfeng.secondhandtradingplatform.constant.RedisConstant.LOGIN_TOKEN_KEY;

@Component
public class UserUtils {

    private static StringRedisTemplate stringRedisTemplate;

    public UserUtils(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public static boolean checkPhone(String phone){
        //判断手机号码是否为空或null
        if(phone == null || phone.isEmpty()){
            return false;
        }
        //判断手机号码是否符合格式
        if (!phone.matches("^1[3456789]\\d{9}$")) {
            return false;
        }

        return true;
    }

    //根据token获取用户id
    public static Long getIdByToken(String token){
        String tokenKey = LOGIN_TOKEN_KEY + token;
        String userIdStr = (String)stringRedisTemplate.opsForHash().get(tokenKey,"id");
        return userIdStr != null ? Long.parseLong(userIdStr) : null;
    }
}
