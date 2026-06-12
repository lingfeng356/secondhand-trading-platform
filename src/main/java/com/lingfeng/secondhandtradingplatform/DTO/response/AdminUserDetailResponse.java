package com.lingfeng.secondhandtradingplatform.DTO.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class AdminUserDetailResponse {

    private Long id;
    private String username;
    private Integer sex;
    private String phone;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String img;
    private String address;
    private String role;
    private Integer status;
}
