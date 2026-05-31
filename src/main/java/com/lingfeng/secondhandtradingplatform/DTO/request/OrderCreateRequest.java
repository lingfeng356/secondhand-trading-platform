package com.lingfeng.secondhandtradingplatform.DTO.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "创建订单请求")
public class OrderCreateRequest {

    @NotNull
    private Long productId;

    @NotNull
    @Min(value = 0)
    @Max(value = 999)
    private Integer quantity;
}
