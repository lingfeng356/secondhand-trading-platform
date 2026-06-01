package com.lingfeng.secondhandtradingplatform.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
@Schema(description = "状态筛选获取我的订单请求")
public class GetMyListByStatusRequest {


    @Schema(description = "页码", defaultValue = "1", example = "1")
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数", defaultValue = "10", example = "10")
    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数最大为100")
    private Integer pageSize = 10;

    @Schema(description = "订单状态",defaultValue = "1",example = "1")
    @Min(value = 0,message = "商品状态最小为1")
    @Max(value = 5,message = "商品状态最大为6")
    @NotNull
    private Integer status;
}
