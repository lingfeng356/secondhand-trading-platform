package com.lingfeng.secondhandtradingplatform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingfeng.secondhandtradingplatform.DTO.Result;
import com.lingfeng.secondhandtradingplatform.DTO.UserDTO;
import com.lingfeng.secondhandtradingplatform.mapper.UserMapper;
import com.lingfeng.secondhandtradingplatform.pojo.User;
import com.lingfeng.secondhandtradingplatform.service.LoginService;
import com.lingfeng.secondhandtradingplatform.util.UserUtils;
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

    @Override
    public Result login(String phone, String code) {
        //确认手机号是否符合格式
        if(!UserUtils.checkPhone(phone)){
            log.info("正在确认手机号:{}是否符合格式",phone);
            return Result.error("手机号格式错误,请重新输入");
        }

        //校验验证码是否正确
        String realCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        log.info("正在确认验证码是否正确");
        if(realCode == null || !realCode.equals(code)){
            log.info("验证码错误");
            return Result.error("验证码错误,请重新登录");
        }

        //登录成功后删除redis中的验证码
        stringRedisTemplate.delete(LOGIN_CODE_KEY + phone);

        //查询数据库确认用户是否存在
        User user = query().eq("phone",phone).one();
        log.info("正在查询手机号为:{}的用户是否存在",phone);
        //如果用户为null,自动创建并保存新用户
        if(user == null){
            user = createUserWithPhone(phone);
            log.info("用户不存在，正在自动创建并保存用户");
        }

        //随机生成一个token作为当前用户的登陆令牌
        String token = UUID.randomUUID().toString(true);
        log.info("正在生成登录令牌,登录令牌为:{}",token);

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
        //TODO:一个用户只能有一个token，现在问题是一个用户反复登录会导致多个token产生
        String tokenKey = LOGIN_TOKEN_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey,userMap);
        //设置时效性
        stringRedisTemplate.expire(tokenKey,LOGIN_USER_TTL,TimeUnit.MINUTES);
        //将token返回前端和用户
        log.info("用户手机号:{}登录成功",phone);

        return Result.success(token);
    }

    @Override
    public Result sendCode(String phone) {
        //确认手机号是否符合格式
        if(!UserUtils.checkPhone(phone)){
            log.info("正在确认手机号:{}是否符合格式",phone);
            return Result.error("手机号格式错误,请重新输入");
        }

        //发送验证码
        log.info("正在生成验证码...");
        String code = RandomUtil.randomNumbers(6);

        //将验证码存储到redis中
        stringRedisTemplate.opsForValue()
                            .set(LOGIN_CODE_KEY + phone,code,LOGIN_CODE_TTL, TimeUnit.SECONDS);
        log.info("用户手机号:{}验证码生成成功,验证码为:{},请在60秒内完成登录",phone,code);

        return Result.success();
    }

    private User createUserWithPhone(String phone){
        User user = new User();
        user.setPhone(phone);
        log.info("正在添加用户,用户手机号为:{}",phone);
        user.setUsername("用户" + RandomUtil.randomString(11));
        log.info("正在添加用户,用户名称为:{}",user.getUsername());
        user.setPassword("123456");
        log.info("正在添加用户,用户密码为:{}",user.getPassword());
        save(user);
        return user;
    }
}
