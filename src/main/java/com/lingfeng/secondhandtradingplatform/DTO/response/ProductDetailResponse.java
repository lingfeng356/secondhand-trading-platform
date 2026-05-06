package com.lingfeng.secondhandtradingplatform.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Schema(description = "商品详情响应")
public class ProductDetailResponse {

    // ===== 商品信息 =====
    @Schema(description = "商品iD")
    private Long id;

    @Schema(description = "商品标题")
    private String title;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "商品分类")
    private String category;

    @Schema(description = "售价")
    private BigDecimal price;

    @Schema(description = "原价")
    private BigDecimal originalPrice;

    @Schema(description = "商品图片")
    private String images;

    @Schema(description = "商品成色")
    private Integer productCondition;

    @Schema(description = "商品状态")
    private Integer status;

    @Schema(description = "所在城市")
    private String city;

    @Schema(description = "交易方式")
    private Integer tradeType;

    @Schema(description = "浏览次数")
    private Integer viewCount;

    @Schema(description = "点赞次数")
    private Integer likeCount;

    @Schema(description = "发布时间")
    private LocalDateTime createTime;

    // ===== 卖家信息（关联查询）=====
    @Schema(description = "卖家ID")
    private Long userId;

    @Schema(description = "卖家昵称")
    private String sellerName;

    @Schema(description = "卖家头像")
    private String sellerImg;
}