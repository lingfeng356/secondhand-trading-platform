package com.lingfeng.secondhandtradingplatform.service.impl;


import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingfeng.secondhandtradingplatform.DTO.request.ChangePasswordRequest;
import com.lingfeng.secondhandtradingplatform.DTO.request.RegisterRequest;
import com.lingfeng.secondhandtradingplatform.DTO.request.ResetPasswordRequest;
import com.lingfeng.secondhandtradingplatform.DTO.Result;
import com.lingfeng.secondhandtradingplatform.DTO.request.UpdateUserRequest;
import com.lingfeng.secondhandtradingplatform.DTO.response.UserDetailResponse;
import com.lingfeng.secondhandtradingplatform.converter.UserConverter;
import com.lingfeng.secondhandtradingplatform.mapper.UserMapper;
import com.lingfeng.secondhandtradingplatform.pojo.User;
import com.lingfeng.secondhandtradingplatform.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

import static com.lingfeng.secondhandtradingplatform.constant.RedisConstant.*;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl extends ServiceImpl<UserMapper,User> implements UserService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("${upload.path}")
    private String uploadPath; // 例如: /uploads/avatars/

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserConverter userConverter;

    //查询用户
    //TODO:区分返回自己信息和他人信息
    @Override
    public Result<UserDetailResponse> getByUserId(Long userId) {

        log.info("获取用户信息请求:userId={}",userId);

        //查询数据库
        User user = query().eq("id",userId).one();

        //判断user是否存在
        if(user == null){
            log.warn("获取用户信息失败:用户不存在,userId={}",userId);
            return Result.error(404,"用户不存在");
        }

        UserDetailResponse udr = userConverter.toUserDetailResponse(user);

        //返回user信息给前端
        log.info("获取用户信息成功:userId={}",userId);
        return Result.success(udr);
    }

    //更新用户
    @Override
    public Result<Void> updateUser(Long userId, UpdateUserRequest uur) {

        log.info("编辑用户请求:userId={}",userId);

        //查询数据库判断是否有该用户
        User hasUser = getById(userId);
        if(hasUser == null){
            log.warn("编辑用户失败:用户不存在,userId={}",userId);
            return Result.error(404,"用户不存在");
        }

        //将请求对象转换为user对象
        User user = userConverter.toEntity(uur);

        //更新数据库信息
        //因为前端传回的userId为null，所以需要手动传值
        user.setId(userId);
        updateById(user);

        //删除redis缓存
        stringRedisTemplate.delete(LOGIN_USER_KEY + userId);

        //返回结果
        log.info("编辑用户成功:userId={}",userId);
        return Result.success();
    }

    //上传图片
    @Override
    //TODO:看不懂喵
    public Result<Void> upload(MultipartFile file, Long userId) {

        log.info("上传图片请求:userId={}",userId);

        try {
            // 1. 校验文件
            if (file == null || file.isEmpty()) {
                log.error("上传图片失败:文件不存在,userId={}",userId);
                return Result.error(404,"文件不能为空");
            }

            // 2. 校验文件类型（只允许图片）
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                log.error("上传图片失败:文件类型错误,userId={}",userId);
                return Result.error(400,"只能上传图片文件");
            }

            // 3. 校验文件大小（例如限制 5MB）
            if (file.getSize() > 5 * 1024 * 1024) {
                log.error("上传图片失败:文件过大,userId={}",userId);
                return Result.error(400,"文件大小不能超过5MB");
            }

            // 4. 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFileName = userId + "_" + System.currentTimeMillis() + extension;

            // 5. 创建目录（如果不存在）
            File destDir = new File(uploadPath);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            // 6. 保存文件到本地
            File destFile = new File(destDir, newFileName);
            file.transferTo(destFile);

            // 7. 存储相对路径到数据库（用于前端访问）
            String dbPath = "/static/" + newFileName;  // 根据你的访问路径调整

            User user = new User();
            user.setId(userId);
            user.setImg(dbPath);
            updateById(user);

            log.info("上传图片成功:userId={}",userId);
            return Result.success();

        } catch (IOException e) {
            log.error("上传图片失败:发生未知错误,userId={}",userId);
            return Result.error(400,"上传失败: " + e.getMessage());
        }
    }

    //修改密码
    @Override
    public Result<Void> changePassword(ChangePasswordRequest request, Long userId) {
        String phone = request.getPhone();
        String code = request.getCode();
        String newPassword = request.getNewPassword();
        String codeKey = LOGIN_CODE_KEY + phone;

        log.info("修改密码请求:phone={}",phone);

        //校验验证码
        String realCode = stringRedisTemplate.opsForValue().get(codeKey);
        if(realCode == null || !realCode.equals(code)){
            log.warn("修改密码失败:验证码错误,phone={}", phone);
            return Result.error(400,"验证码错误");
        }

        //根据手机号去数据库查询是否有该用户
        User user = query().eq("phone",phone).one();
        if(user == null){
            log.warn("重置密码失败:用户不存在,phone={}", phone);
            return Result.error(404,"用户不存在");
        }

        //重置密码
        user.setPassword(newPassword);

        //更新数据库
        updateById(user);

        //重置成功后删除redis中的验证码缓存
        stringRedisTemplate.delete(codeKey);

        //返回结果
        log.info("修改密码成功:phone={}", phone);
        return Result.success();
    }

    //退出登录
    @Override
    public Result<Void> logout(Long userId) {

        log.info("退出登录请求:userId={}",userId);

        StpUtil.logout();

        //删除token
        String userKey = LOGIN_USER_KEY + userId;
        stringRedisTemplate.delete(userKey);

        //返回结果
        log.info("退出登录成功:userId={}",userId);
        return Result.success();
    }

    //重置密码
    @Override
    public Result<Void> resetPwd(ResetPasswordRequest rpr) {
        String phone = rpr.getPhone();
        String code = rpr.getCode();
        String newPassword = rpr.getNewPassword();
        String codeKey = LOGIN_CODE_KEY + phone;

        log.info("重置密码请求:phone={}",phone);

        //校验验证码
        String realCode = stringRedisTemplate.opsForValue().get(codeKey);
        if(realCode == null || !realCode.equals(code)){
            log.warn("重置密码失败:验证码错误,phone={}", phone);
            return Result.error(400,"验证码错误");
        }

        //根据手机号去数据库查询是否有该用户
        User user = query().eq("phone",phone).one();
        if(user == null){
            log.warn("重置密码失败:用户不存在,phone={}", phone);
            return Result.error(404,"用户不存在");
        }

        //重置密码
        user.setPassword(newPassword);

        //更新数据库
        updateById(user);

        //重置成功后删除redis中的验证码缓存
        stringRedisTemplate.delete(codeKey);

        //返回结果
        log.info("重置密码成功:phone={}", phone);
        return Result.success();
    }

    //用户注册
    @Override
    public Result<String> register(RegisterRequest rr) {
        //获取参数
        String phone = rr.getPhone();
        String code = rr.getCode();
        String username = rr.getUsername();
        Integer sex = rr.getSex();
        String password = rr.getPassword();
        String img = rr.getImg();
        String codeKey = LOGIN_CODE_KEY + phone;

        log.info("用户注册请求:phone={}",phone);

        //校验验证码
        String realCode = stringRedisTemplate.opsForValue().get(codeKey);
        if(realCode == null || !realCode.equals(code)){
            log.warn("用户注册失败:验证码错误,phone={}", phone);
            return Result.error(400,"验证码错误");
        }

        //查询数据库是否有用户
        User hasUser = query().eq("phone",phone).one();
        if(hasUser != null){
            log.warn("用户注册失败:用户已注册,phone={}",phone);
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

        Long userId = user.getId();

        //注册成功后直接登录
        StpUtil.login(userId);
        String token = StpUtil.getTokenValue();

        //删除redis中验证码缓存
        stringRedisTemplate.delete(codeKey);

        //返回结果
        log.info("用户注册成功:phone={}",phone);
        return Result.success(token);
    }

}
