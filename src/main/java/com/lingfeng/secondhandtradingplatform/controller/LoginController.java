package com.lingfeng.secondhandtradingplatform.controller;


import com.lingfeng.secondhandtradingplatform.DTO.*;
import com.lingfeng.secondhandtradingplatform.DTO.request.LoginByCodeRequest;
import com.lingfeng.secondhandtradingplatform.DTO.request.LoginByPasswordRequest;
import com.lingfeng.secondhandtradingplatform.DTO.request.SendCodeRequest;
import com.lingfeng.secondhandtradingplatform.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController//要用@RestController才会返回json格式，否则只会跳转页面
@Tag(name = "登录模块")
@RequestMapping("/user")
public class LoginController {

    @Autowired
    private LoginService loginService;

    //验证码登录
    @PostMapping("/loginByCode")
    @Operation(summary = "验证码登录")
    @ApiResponses({
            @ApiResponse(responseCode = "400",description = "验证码无效"),
            @ApiResponse(responseCode = "404",description = "用户不存在")
    })
    public Result loginByCode(@RequestBody LoginByCodeRequest lbcr){
        String phone = lbcr.getPhone();
        String code = lbcr.getCode();
        return loginService.loginByCode(phone,code);
    }

    //发送验证码
    @PostMapping("/sendCode")
    @Operation(summary = "发送验证码")
    public Result sendCode(@RequestBody SendCodeRequest scr){
        String phone = scr.getPhone();
        return loginService.sendCode(phone);
    }

    //密码登录
    @PostMapping("/loginByPassword")
    @Operation(summary = "密码登录")
    @ApiResponses({
            @ApiResponse(responseCode = "404",description = "用户不存在"),
            @ApiResponse(responseCode = "400",description = "密码错误")
    })
    public Result loginByPassword(@RequestBody LoginByPasswordRequest lbpr){
        String phone = lbpr.getPhone();
        String password = lbpr.getPassword();
        return loginService.loginByPassword(phone,password);
    }

}
