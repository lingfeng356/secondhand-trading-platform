package com.lingfeng.secondhandtradingplatform.controller;


import com.lingfeng.secondhandtradingplatform.DTO.LoginRequest;
import com.lingfeng.secondhandtradingplatform.DTO.Result;
import com.lingfeng.secondhandtradingplatform.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
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
    public Result loginByCode(@RequestBody LoginRequest loginRequest){
        String phone = loginRequest.getPhone();
        String code = loginRequest.getCode();
        return loginService.loginByCode(phone,code);
    }

    //发送验证码
    @PostMapping("/sendCode")
    public Result sendCode(@RequestBody LoginRequest loginRequest){
        String phone = loginRequest.getPhone();
        return loginService.sendCode(phone);
    }

    //密码登录
    @PostMapping("/loginByPassword")
    public Result loginByPassword(@RequestBody LoginRequest loginRequest){
        String phone = loginRequest.getPhone();
        String password = loginRequest.getPassword();
        return loginService.loginByPassword(phone,password);
    }

}
