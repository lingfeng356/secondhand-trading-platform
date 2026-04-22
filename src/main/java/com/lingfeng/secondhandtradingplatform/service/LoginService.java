package com.lingfeng.secondhandtradingplatform.service;

import com.lingfeng.secondhandtradingplatform.DTO.Result;

public interface LoginService {

    Result login(String phone,String code);

    Result sendCode(String phone);
}
