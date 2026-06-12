package com.lingfeng.secondhandtradingplatform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lingfeng.secondhandtradingplatform.DTO.Result;
import com.lingfeng.secondhandtradingplatform.DTO.request.PageRequest;
import com.lingfeng.secondhandtradingplatform.DTO.response.AdminUserDetailResponse;

public interface AdminUserService {
    Result<IPage<AdminUserDetailResponse>> getUserList(PageRequest request);

    Result<Void> banUser(Long userId);

    Result<Void> unBanUser(Long userId);
}
