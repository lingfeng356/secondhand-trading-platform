package com.lingfeng.secondhandtradingplatform.controller;


import com.lingfeng.secondhandtradingplatform.DTO.Result;
import com.lingfeng.secondhandtradingplatform.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController//要用@RestController才会返回json格式，否则只会跳转页面
public class LoginController {

    @Autowired
    private LoginService loginService;

    //验证码登录
    @GetMapping("/user/loginByCode")
    public Result loginByCode(String phone,String code){
        return loginService.loginByCode(phone,code);
    }

    //发送验证码
    @PostMapping("/user/sendCode")
    public Result sendCode(@RequestParam String phone){
        return loginService.sendCode(phone);
    }

    @GetMapping("/user/loginByPassword")
    public Result loginByPassword(String phone,String password){
        return loginService.loginByPassword(phone,password);
    }

}
