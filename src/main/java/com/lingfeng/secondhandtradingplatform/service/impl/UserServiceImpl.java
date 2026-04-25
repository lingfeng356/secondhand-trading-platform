package com.lingfeng.secondhandtradingplatform.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingfeng.secondhandtradingplatform.DTO.Result;
import com.lingfeng.secondhandtradingplatform.mapper.UserMapper;
import com.lingfeng.secondhandtradingplatform.pojo.User;
import com.lingfeng.secondhandtradingplatform.service.UserService;
import com.lingfeng.secondhandtradingplatform.util.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.lingfeng.secondhandtradingplatform.constant.RedisConstant.LOGIN_TOKEN_KEY;
import static com.lingfeng.secondhandtradingplatform.constant.RedisConstant.LOGIN_USER_TTL;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper,User> implements UserService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    //从redis中获取用户id,然后根据id查询用户信息
    @Override
    public Result getByToken(String token) {
        // 在 getByToken 方法开头加一行
        //trim()作用是去除字符串首尾的空格，防止"   "这种token被放行
        if (token == null || token.trim().isEmpty()) {
            return Result.error("未登录");
        }
        //获取用户id
        Long userId = UserUtils.getIdByToken(token);
        log.info("正在查询用户id,用户id为{}",userId);
        //判断id是否为null
        if(userId == null){
            return Result.error("登录信息过期，请重新登录");
        }
        //查询数据库
        User user = query().eq("id",userId).one();
        log.info("正在查询用户");
        //判断user是否存在
        if(user == null){
            log.info("用户不存在");
            return Result.error("用户不存在");
        }
        log.info("用户存在");
        //返回user信息给前端
        return Result.success(user);
    }

    //从redis中获取用户id,然后根据id更新用户信息
    @Override
    public Result updateByToken(String token, User user) {
        //查询用户id
        Long userId = UserUtils.getIdByToken(token);
        log.info("正在查询用户，用户id为{}",userId);
        //先判断id是否为null
        if(userId == null){
            log.info("id为null,操作失败");
            return Result.error("登录信息已过期，请重试");
        }
        //更新数据库信息
        //因为前端传回的userId为null，所以需要手动传值
        user.setId(userId);
        boolean updated = updateById(user);
        log.info("正在更新数据库");
        //判断是否更新成功
        if(!updated){
            log.info("未知原因，更新失败");
            return Result.error("更新失败，请重试");
        }
        //更新redis缓存
        String tokenKey = LOGIN_TOKEN_KEY + token;
        Map<String,String> userMap = new HashMap<>();
        userMap.put("id",userId.toString());
        userMap.put("phone",user.getPhone());
        userMap.put("img",user.getImg());
        stringRedisTemplate.opsForHash().putAll(tokenKey,userMap);
        stringRedisTemplate.expire(tokenKey,LOGIN_USER_TTL, TimeUnit.MINUTES);
        log.info("redis缓存已更新");
        //返回结果
        return Result.success();
    }
}
