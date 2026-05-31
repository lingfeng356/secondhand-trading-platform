package com.lingfeng.secondhandtradingplatform.message;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class OrderItemCreateMessage {

    private String orderId;
    private Long productId;
    private String productName;
    private String productImage;
    private BigDecimal amount;
    private BigDecimal price;
    private Integer Quantity;
}
