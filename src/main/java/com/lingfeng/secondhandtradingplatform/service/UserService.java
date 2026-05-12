package com.lingfeng.secondhandtradingplatform.service;


import com.lingfeng.secondhandtradingplatform.DTO.request.RegisterRequest;
import com.lingfeng.secondhandtradingplatform.DTO.request.ResetPasswordRequest;
import com.lingfeng.secondhandtradingplatform.DTO.Result;
import com.lingfeng.secondhandtradingplatform.DTO.request.UpdateUserRequest;
import com.lingfeng.secondhandtradingplatform.DTO.response.UserDetailResponse;

public interface UserService {

    Result<UserDetailResponse> getByUserId(Long userId);

    Result<Void> logout(Long userId);

    Result<Void> resetPwd(ResetPasswordRequest rpr);

    Result<String> register(RegisterRequest rr);

    Result<Void> updateUser(Long userId, UpdateUserRequest uur);
}
