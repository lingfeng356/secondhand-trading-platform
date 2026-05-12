package com.lingfeng.secondhandtradingplatform.controller;


import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.lingfeng.secondhandtradingplatform.DTO.request.RegisterRequest;
import com.lingfeng.secondhandtradingplatform.DTO.request.ResetPasswordRequest;
import com.lingfeng.secondhandtradingplatform.DTO.Result;
import com.lingfeng.secondhandtradingplatform.DTO.request.UpdateUserRequest;
import com.lingfeng.secondhandtradingplatform.DTO.response.UserDetailResponse;
import com.lingfeng.secondhandtradingplatform.pojo.User;
import com.lingfeng.secondhandtradingplatform.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "用户模块")
@RequestMapping("/user")
@Validated  // 加在类上，类中所有方法参数都会自动校验
public class UserController {

    @Autowired
    private UserService userService;

    //查询用户信息
    @GetMapping("/getUser")
    @Operation(summary = "获取用户信息")
    @ApiResponses({
            @ApiResponse(responseCode = "404",description = "用户不存在")
    })
    public Result<UserDetailResponse> getByUserId(@RequestParam(required = false) Long userId){
        //获取用户id,如果前端没传userId，则从threadLocal中获取
        if(userId == null){
            userId = StpUtil.getLoginIdAsLong();
        }
        return userService.getByUserId(userId);
    }

    //编辑用户信息
    @SaCheckLogin
    @PostMapping("/updateUser")
    @Operation(summary = "编辑用户信息")
    @ApiResponse(responseCode = "404",description = "用户不存在")
    public Result<Void> updateUser(@RequestBody UpdateUserRequest uur){
        //查询用户id
        Long userId = StpUtil.getLoginIdAsLong();
        return userService.updateUser(userId,uur);
    }

    //用户注册
    @PostMapping("/register")
    @Operation(summary = "用户注册")
    @ApiResponses({
            @ApiResponse(responseCode = "400",description = "验证码错误"),
            @ApiResponse(responseCode = "409",description = "用户已注册")
    })
    public Result<String> register(@RequestBody RegisterRequest rr){
        return userService.register(rr);
    }

    //退出登录
    @PostMapping("/logout")
    @Operation(summary = "用户退出登录")
    @ApiResponses(@ApiResponse(responseCode = "401",description = "未登录"))
    public Result<Void> logout(){
        //获取userId
        Long userId = StpUtil.getLoginIdAsLong();
        return userService.logout(userId);
    }

    //重置密码
    @PostMapping("/resetPwd")
    @Operation(summary = "用户重置密码")
    @ApiResponses({
            @ApiResponse(responseCode = "400",description = "验证码错误"),
            @ApiResponse(responseCode = "404",description = "用户不存在")
    })
    public Result<Void> resetPwd(@RequestBody ResetPasswordRequest rpr){
        return userService.resetPwd(rpr);
    }

    //上传头像
}
