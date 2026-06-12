package com.lingfeng.secondhandtradingplatform.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingfeng.secondhandtradingplatform.DTO.Result;
import com.lingfeng.secondhandtradingplatform.DTO.request.PageRequest;
import com.lingfeng.secondhandtradingplatform.DTO.response.AdminUserDetailResponse;
import com.lingfeng.secondhandtradingplatform.converter.UserConverter;
import com.lingfeng.secondhandtradingplatform.mapper.UserMapper;
import com.lingfeng.secondhandtradingplatform.pojo.User;
import com.lingfeng.secondhandtradingplatform.service.AdminUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class AdminUserServiceImpl implements AdminUserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserConverter userConverter;

    //获取用户列表
    @Override
    public Result<IPage<AdminUserDetailResponse>> getUserList(PageRequest request) {

        Long adminId = StpUtil.getLoginIdAsLong();
        Integer pageNum = request.getPageNum();
        Integer pageSize = request.getPageSize();

        log.info("管理员{}正在获取用户列表",adminId);

        IPage<User> page = new Page<>(pageNum,pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(User::getCreateTime);

        IPage<User> userPage = userMapper.selectPage(page,wrapper);

        IPage<AdminUserDetailResponse> voPage = userPage.convert(user -> {
            AdminUserDetailResponse vo = userConverter.toAdminUserDetailResponse(user);
            return vo;
        });

        log.info("管理员{}获取用户列表成功",adminId);
        return Result.success(voPage);
    }

    //用户封禁
    @Override
    public Result<Void> banUser(Long userId) {
        Long adminId = StpUtil.getLoginIdAsLong();

        log.info("管理员{}正在封禁用户{}",adminId,userId);

        User user = userMapper.selectById(userId);

        if (user == null) {
            log.warn("封禁失败:用户不存在, adminId={}, userId={}", adminId, userId);
            return Result.error(404, "用户不存在");
        }

        if(user.getStatus().equals(1)){
            log.warn("封禁失败:已封禁,adminId={},userId={}",adminId,userId);
            return Result.error(400, "用户已被封禁");
        }

        user.setStatus(1);
        userMapper.updateById(user);

        StpUtil.logout(userId);

        log.info("管理员{}封禁用户{}成功", adminId, userId);
        return Result.success();
    }

    //解封用户
    @Override
    public Result<Void> unBanUser(Long userId) {
        Long adminId = StpUtil.getLoginIdAsLong();

        log.info("管理员{}正在解封用户{}",adminId,userId);

        User user = userMapper.selectById(userId);

        if (user == null) {
            log.warn("解封失败:用户不存在, adminId={}, userId={}", adminId, userId);
            return Result.error(404, "用户不存在");
        }

        if(user.getStatus().equals(0)){
            log.warn("解封失败:用户已是正常状态, adminId={}, userId={}", adminId, userId);
            return Result.error(400, "用户已是正常状态");
        }

        user.setStatus(0);
        userMapper.updateById(user);

        log.info("管理员{}解封用户{}成功", adminId, userId);
        return Result.success();
    }
}
