package com.lingfeng.secondhandtradingplatform.service;

import com.lingfeng.secondhandtradingplatform.DTO.Result;

public interface LoginService {

    //验证码登录
    Result<String> loginByCode(String phone,String code);
    //发送验证码
    Result<String> sendCode(String phone);
    //密码登录
    Result<String> loginByPassword(String phone,String password);
}
