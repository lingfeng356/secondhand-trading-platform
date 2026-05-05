package com.lingfeng.secondhandtradingplatform.controller;


import com.lingfeng.secondhandtradingplatform.DTO.RegisterRequest;
import com.lingfeng.secondhandtradingplatform.DTO.ResetPasswordRequest;
import com.lingfeng.secondhandtradingplatform.DTO.Result;
import com.lingfeng.secondhandtradingplatform.pojo.User;
import com.lingfeng.secondhandtradingplatform.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Tag(name = "用户模块")
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    //查询用户信息
    @GetMapping("/getUser")
    public Result getByToken(@RequestHeader("token") String token){
        return userService.getByToken(token);
    }

    //编辑用户信息
    @PostMapping("/updateUser")
    public Result updateByToken(@RequestHeader("token") String token,@RequestBody User user){
        return userService.updateByToken(token,user);
    }

    //用户注册
    @PostMapping("/register")
    @Operation(summary = "用户注册")
    @ApiResponses({
            @ApiResponse(responseCode = "400",description = "验证码错误"),
            @ApiResponse(responseCode = "409",description = "用户已注册")
    })
    public Result register(@RequestBody RegisterRequest rr){
        return userService.register(rr);
    }

    //退出登录
    @PostMapping("/logout")
    @Operation(summary = "用户退出登录")
    @ApiResponses(@ApiResponse(responseCode = "401",description = "未登录"))
    public Result logout(@RequestHeader("token") String token){
        return userService.logout(token);
    }

    //重置密码
    @PostMapping("/resetPwd")
    @Operation(summary = "用户重置密码")
    @ApiResponses({
            @ApiResponse(responseCode = "400",description = "验证码错误"),
            @ApiResponse(responseCode = "404",description = "用户不存在")
    })
    public Result resetPwd(@RequestBody ResetPasswordRequest rpr){
        return userService.resetPwd(rpr);
    }

    //上传头像
}
