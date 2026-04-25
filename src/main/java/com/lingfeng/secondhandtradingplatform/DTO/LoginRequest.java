package com.lingfeng.secondhandtradingplatform.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginRequest {
    private String phone;
    private String code;
    private String password;
}
