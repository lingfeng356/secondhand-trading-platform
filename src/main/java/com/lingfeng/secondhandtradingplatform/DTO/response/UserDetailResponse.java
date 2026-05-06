package com.lingfeng.secondhandtradingplatform.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "用户详情响应")
public class UserDetailResponse {

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "用户名", example = "张三")
    private String username;

    @Schema(description = "性别", example = "1")
    private String sex;

    @Schema(description = "头像URL", example = "/img/avatar.png")
    private String img;

    @Schema(description = "地址", example = "广东省深圳市南山区")
    private String address;
}