package com.lingfeng.secondhandtradingplatform.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingfeng.secondhandtradingplatform.DTO.Result;
import com.lingfeng.secondhandtradingplatform.DTO.UserDTO;
import com.lingfeng.secondhandtradingplatform.mapper.UserMapper;
import com.lingfeng.secondhandtradingplatform.pojo.User;
import com.lingfeng.secondhandtradingplatform.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.lingfeng.secondhandtradingplatform.constant.RedisConstant.*;

@Slf4j
@Service
public class LoginServiceImpl extends ServiceImpl<UserMapper,User> implements LoginService{

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserMapper userMapper;

    //验证码登录
    @Override
    public Result<String> loginByCode(String phone, String code) {

        log.info("验证码登录请求:phone={}",phone);

        String codeKey = LOGIN_CODE_KEY + phone;

        //校验验证码是否正确
        String realCode = stringRedisTemplate.opsForValue().get(codeKey);
        if(realCode == null || !realCode.equals(code)){
            log.warn("验证码登录失败:验证码无效,phone={}",phone);
            return Result.error(400,"验证码无效");
        }

        //登录成功后删除redis中的验证码
        stringRedisTemplate.delete(codeKey);

        //查询数据库确认用户是否存在
        User user = query().eq("phone",phone).one();

        //如果用户为null,返回报错
        if(user == null){
            log.warn("验证码登录失败:用户不存在,phone={}",phone);
            return Result.error(404,"用户不存在");
        }

        Long userId = user.getId();

        //使用satoken生成token进行登录
        StpUtil.login(userId);
        String token = StpUtil.getTokenValue();

        //将user对象转变为userDTO对象,防止私密信息泄露
        UserDTO userDTO = BeanUtil.copyProperties(user,UserDTO.class);

        //将userDTO对象转换为hashmap保存到redis中,查询基本信息时可以快速查询缓存获取
        Map<String,Object> tempMap = BeanUtil.beanToMap(userDTO);
        Map<String,String> userMap = new HashMap<>();

        //将其中的对象全都转化为String类型
        for (Map.Entry<String, Object> entry : tempMap.entrySet()) {
            if (entry.getValue() != null) {
                userMap.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        String userKey = LOGIN_USER_KEY + userId;
        stringRedisTemplate.opsForHash().putAll(userKey,userMap);

        //设置时效性
        stringRedisTemplate.expire(userKey,LOGIN_USER_TTL,TimeUnit.MINUTES);

        //将token返回前端和用户
        log.info("验证码登录成功:phone={}",phone);
        return Result.success(token);
    }

    //发送验证码
    @Override
    public Result<String> sendCode(String phone) {
        //发送验证码
        log.info("发送验证码请求:phone={}",phone);

        String limitKey = LOGIN_LIMIT_KEY + phone;
        Long count = stringRedisTemplate.opsForValue().increment(limitKey);
        if(count == 1){
            stringRedisTemplate.expire(limitKey,LOGIN_LIMIT_TTL,TimeUnit.MINUTES);
        }else if(count > 1){
            log.warn("发送验证码失败:发送过快,phone:{}",phone);
            return Result.error(429, "发送频率过快，请1分钟后重试");
        }

        String code = RandomUtil.randomNumbers(6);

        //将验证码存储到redis中
        stringRedisTemplate.opsForValue()
                            .set(LOGIN_CODE_KEY + phone,code,LOGIN_CODE_TTL, TimeUnit.SECONDS);

        //返回成功信息
        log.info("发送验证码成功:phone={},code={}",phone,code);
        return Result.success();
    }

    //密码登录
    @Override
    public Result<String> loginByPassword(String phone, String password) {

        log.info("密码登录请求:phone={}",phone);

        //查询数据库中用户的密码
        User user = query().eq("phone",phone).one();

        //如果用户为null,返回错误
        if(user == null){
            log.warn("密码登录失败:用户不存在,phone={}",phone);
            return Result.error(404,"用户不存在");
        }

        //如果用户存在,比较密码
        String realPassword = user.getPassword();
        //如果密码不正确,返回报错
        if(realPassword == null || !realPassword.equals(password)){
            log.warn("密码登录失败:密码错误,phone={}",phone);
            return Result.error(400,"密码错误");
        }

        Long userId = user.getId();

        //使用satoken生成token进行登录
        StpUtil.login(userId);
        String token = StpUtil.getTokenValue();

        //将user对象转变为userDTO对象,防止私密信息泄露
        UserDTO userDTO = BeanUtil.copyProperties(user,UserDTO.class);

        //将userDTO对象转换为hashmap保存到redis中
        Map<String,Object> tempMap = BeanUtil.beanToMap(userDTO);
        Map<String,String> userMap = new HashMap<>();

        //将其中的对象全都转化为String类型
        for (Map.Entry<String, Object> entry : tempMap.entrySet()) {
            if (entry.getValue() != null) {
                userMap.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        String userKey = LOGIN_USER_KEY + userId;
        stringRedisTemplate.opsForHash().putAll(userKey,userMap);

        //设置时效性
        stringRedisTemplate.expire(userKey,LOGIN_USER_TTL,TimeUnit.MINUTES);

        //将token返回前端和用户
        log.info("密码登录成功:phone={}",phone);
        return Result.success(token);
    }
}
