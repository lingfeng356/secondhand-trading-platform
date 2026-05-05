package com.lingfeng.secondhandtradingplatform.service;


import com.lingfeng.secondhandtradingplatform.DTO.RegisterRequest;
import com.lingfeng.secondhandtradingplatform.DTO.ResetPasswordRequest;
import com.lingfeng.secondhandtradingplatform.DTO.Result;
import com.lingfeng.secondhandtradingplatform.pojo.User;

public interface UserService {

    Result getByToken(String token);

    Result updateByToken(String token,User user);

    Result logout(String token);

    Result resetPwd(ResetPasswordRequest rpr);

    Result register(RegisterRequest rr);
}
