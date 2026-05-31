package com.lingfeng.secondhandtradingplatform.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Schema(description = "商品发布请求")
public class ProductPublishRequest {

    @Schema(description = "商品标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "商品信息不能为空")
    @Size(min = 1, max = 100, message = "标题长度1-100字")
    private String title;

    @Schema(description = "商品描述", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 100, message = "描述不能超过100字")
    private String description;

    @Schema(description = "商品分类", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "商品信息不能为空")
    private String category;

    @Schema(description = "售价", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "商品信息不能为空")
    @DecimalMin(value = "0.01", message = "售价必须大于0,小于999999.99")
    @DecimalMax(value = "999999.99", message = "售价必须大于0,小于999999.99")
    private BigDecimal price;

    @Schema(description = "原价", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @DecimalMin(value = "0.01", message = "售价必须大于0,小于999999.99")
    @DecimalMax(value = "999999.99", message = "售价必须大于0,小于999999.99")
    private BigDecimal originalPrice;

    @Schema(description = "商品图片地址（多个用逗号分隔）", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 1000, message = "图片地址过长")
    private String images;

    @Schema(description = "所在城市", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "商品信息不能为空")
    private String city;

    @Schema(description = "交易方式（1:同城面交, 2:快递邮寄, 3:自提）", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"1", "2", "3"})
    @Min(value = 1, message = "交易方式参数错误")
    @Max(value = 3, message = "交易方式参数错误")
    @NotNull(message = "商品信息不能为空")
    private Integer tradeType;

    @Schema(description = "商品成色（1:全新, 2:几乎全新, 3:有使用痕迹）", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"1", "2", "3"})
    @NotNull(message = "商品信息不能为空")
    @Min(value = 1, message = "成色参数错误")
    @Max(value = 3, message = "成色参数错误")
    private Integer productCondition;

    @Schema(description = "商品库存")
    @Min(value = 0,message = "商品库存数量错误")
    @Max(value = 999,message = "商品库存数量错误")
    private Integer stock;
}