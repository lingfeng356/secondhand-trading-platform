package com.lingfeng.secondhandtradingplatform.service;

import com.lingfeng.secondhandtradingplatform.DTO.Result;

public interface LoginService {

    //验证码登录
    Result loginByCode(String phone,String code);
    //发送验证码
    Result sendCode(String phone);
    //密码登录
    Result loginByPassword(String phone,String password);
}
