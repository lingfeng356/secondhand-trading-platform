package com.lingfeng.secondhandtradingplatform.DTO.response;

import com.lingfeng.secondhandtradingplatform.pojo.OrderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Schema(description = "创建订单响应")
public class OrderDetailResponse {

    // ========== 订单基本信息 ==========

    @Schema(description = "订单ID", example = "a1b2c3d4e5f67890")
    private String orderId;

    @Schema(description = "订单编号", example = "ORD20260506123456789")
    private String orderNo;

    @Schema(description = "订单总金额", example = "6299.00")
    private BigDecimal totalAmount;

    @Schema(description = "实付金额", example = "6299.00")
    private BigDecimal payAmount;

    @Schema(description = "订单状态", example = "1", allowableValues = {"1", "2", "3", "4", "5", "6"})
    private Integer status;

    // ========== 商品列表 ==========

    @Schema(description = "订单商品列表")
    private OrderItem orderItem;

    // ========== 卖家信息 ==========
    @Schema(description = "卖家ID", example = "10001")
    private Long sellerId;

    @Schema(description = "卖家昵称", example = "科技数码店")
    private String sellerName;

    @Schema(description = "卖家头像", example = "/img/avatar/seller.jpg")
    private String sellerImg;

    @Schema(description = "评价状态",example = "0",allowableValues = {"0","1"})
    private Integer isReview;
}