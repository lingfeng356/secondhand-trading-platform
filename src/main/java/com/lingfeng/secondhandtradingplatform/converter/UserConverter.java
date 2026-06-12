package com.lingfeng.secondhandtradingplatform.converter;


import com.lingfeng.secondhandtradingplatform.DTO.request.UpdateUserRequest;
import com.lingfeng.secondhandtradingplatform.DTO.response.AdminUserDetailResponse;
import com.lingfeng.secondhandtradingplatform.DTO.response.UserDetailResponse;
import com.lingfeng.secondhandtradingplatform.pojo.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserConverter {

    User toEntity(UserDetailResponse response);

    UserDetailResponse toUserDetailResponse(User user);

    User toEntity(UpdateUserRequest request);

    UpdateUserRequest toUpdateUserRequest(User user);

    User toEntity(AdminUserDetailResponse response);

    AdminUserDetailResponse toAdminUserDetailResponse(User user);
}
