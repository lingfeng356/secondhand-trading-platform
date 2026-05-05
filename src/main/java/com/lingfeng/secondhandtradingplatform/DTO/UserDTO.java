package com.lingfeng.secondhandtradingplatform.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDTO {
    private Long id;           // 用户ID

    private String username;   // 昵称

    private String img;//用户头像
}
