package com.lingfeng.secondhandtradingplatform.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "更新用户信息请求")
public class UpdateUserRequest {

    @Schema(description = "用户名", example = "张三")
    @Size(min = 2, max = 20, message = "用户名长度2-20位")
    @Pattern(regexp = "^[a-zA-Z0-9\u4e00-\u9fa5]{2,20}$", message = "用户名只能包含中文、英文、数字")
    private String username;

    @Schema(description = "性别", example = "1", allowableValues = {"0", "1", "2"})
    @Min(value = 0, message = "性别参数错误")
    @Max(value = 2, message = "性别参数错误")
    private Integer sex;

    @Schema(description = "头像URL", example = "/img/avatar.png")
    @Size(max = 255, message = "头像地址长度不能超过255")
    private String img;

    @Schema(description = "地址", example = "广东省深圳市南山区")
    @Size(max = 255, message = "地址长度不能超过255")
    private String address;
}