package com.lingfeng.secondhandtradingplatform.controller.adimin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lingfeng.secondhandtradingplatform.DTO.Result;
import com.lingfeng.secondhandtradingplatform.DTO.request.PageRequest;
import com.lingfeng.secondhandtradingplatform.DTO.response.AdminUserDetailResponse;
import com.lingfeng.secondhandtradingplatform.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin")
@Schema(description = "管理用户模块")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    //获取用户列表
    @GetMapping("/getUserList")
    @Operation(summary = "获取用户列表")
    @SaCheckRole("admin")
    public Result<IPage<AdminUserDetailResponse>> getUserList(PageRequest request){
        return adminUserService.getUserList(request);
    }

    //封禁用户
    @PostMapping("/banUser/{userId}")
    @Operation(summary = "封禁用户")
    @SaCheckRole("admin")
    public Result<Void> banUser(@PathVariable("userId") Long userId){
        return adminUserService.banUser(userId);
    }

    //解封用户
    @PostMapping("/unBanUser/{userId}")
    @Operation(summary = "解封用户")
    @SaCheckRole("admin")
    public Result<Void> unBanUser(@PathVariable("userId") Long userId){
        return adminUserService.unBanUser(userId);
    }
}
