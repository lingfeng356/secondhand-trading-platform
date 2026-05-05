package com.lingfeng.secondhandtradingplatform.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingfeng.secondhandtradingplatform.DTO.RegisterRequest;
import com.lingfeng.secondhandtradingplatform.DTO.ResetPasswordRequest;
import com.lingfeng.secondhandtradingplatform.DTO.Result;
import com.lingfeng.secondhandtradingplatform.mapper.UserMapper;
import com.lingfeng.secondhandtradingplatform.pojo.User;
import com.lingfeng.secondhandtradingplatform.service.UserService;
import com.lingfeng.secondhandtradingplatform.util.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.lingfeng.secondhandtradingplatform.constant.RedisConstant.*;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl extends ServiceImpl<UserMapper,User> implements UserService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    //查询用户
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

    //更新用户
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

    //退出登录
    @Override
    public Result logout(String token) {
        //获取userId写日志
        Long userId = UserUtils.getIdByToken(token);

        log.info("退出登录请求:userId={}",userId);

        //删除token
        String tokenKey = LOGIN_TOKEN_KEY + token;
        stringRedisTemplate.delete(tokenKey);
        log.info("退出登录成功:userId={}",userId);

        //返回结果
        return Result.success();
    }

    //重置密码
    @Override
    public Result resetPwd(ResetPasswordRequest rpr) {
        String phone = rpr.getPhone();
        String code = rpr.getCode();
        String newPassword = rpr.getNewPassword();
        String codeKey = LOGIN_CODE_KEY + phone;

        log.info("重置密码请求:phone={}",phone);

        //校验验证码
        String realCode = stringRedisTemplate.opsForValue().get(codeKey);
        if(realCode == null || !realCode.equals(code)){
            log.warn("重置密码失败: phone={}, 验证码错误", phone);
            return Result.error(400,"验证码错误");
        }

        //根据手机号去数据库查询是否有该用户
        User user = query().eq("phone",phone).one();
        if(user == null){
            log.warn("重置密码失败: phone={}, 验证码错误", phone);
            return Result.error(404,"用户不存在");
        }

        //重置密码
        user.setPassword(newPassword);

        //重置成功后删除redis中的验证码缓存
        stringRedisTemplate.delete(codeKey);

        //更新数据库
        updateById(user);

        //返回结果
        log.info("重置密码成功: phone={}", phone);
        return Result.success();
    }

    //用户注册
    @Override
    public Result register(RegisterRequest rr) {
        //获取参数
        String phone = rr.getPhone();
        String code = rr.getCode();
        String username = rr.getUsername();
        String sex = rr.getSex();
        String password = rr.getPassword();
        String img = rr.getImg();
        String codeKey = LOGIN_CODE_KEY + phone;

        log.info("用户注册请求:phone={}",phone);

        //校验验证码
        String realCode = stringRedisTemplate.opsForValue().get(codeKey);
        if(realCode == null || !realCode.equals(code)){
            log.warn("用户注册失败: phone={}, 验证码错误", phone);
            return Result.error(400,"验证码错误");
        }

        //删除redis中验证码缓存
        stringRedisTemplate.delete(codeKey);

        //查询数据库是否有用户
        User hasUser = query().eq("phone",phone).one();
        if(hasUser != null){
            log.warn("用户注册失败:phone={},用户已注册",phone);
            return Result.error(409,"用户已注册");
        }

        //注册用户
        User user = new User();
        user.setPhone(phone);
        user.setUsername(username);
        user.setSex(sex);
        user.setPassword(password);
        user.setImg(img);

        //保存到数据库
        save(user);

        //返回结果
        log.info("用户注册成功:phone={}",phone);
        return Result.success();
    }

}
