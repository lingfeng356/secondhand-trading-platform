package com.lingfeng.secondhandtradingplatform.service;


import com.lingfeng.secondhandtradingplatform.DTO.request.RegisterRequest;
import com.lingfeng.secondhandtradingplatform.DTO.request.ResetPasswordRequest;
import com.lingfeng.secondhandtradingplatform.DTO.Result;
import com.lingfeng.secondhandtradingplatform.DTO.request.UpdateUserRequest;
import com.lingfeng.secondhandtradingplatform.pojo.User;

public interface UserService {

    Result getByUserId(Long userId);

    Result logout();

    Result resetPwd(ResetPasswordRequest rpr);

    Result register(RegisterRequest rr);

    Result updateUser(UpdateUserRequest uur);
}
