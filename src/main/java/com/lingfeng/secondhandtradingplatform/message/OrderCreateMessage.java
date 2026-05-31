package com.lingfeng.secondhandtradingplatform.message;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class OrderCreateMessage {

    private Long userId;
    private Long sellerId;
    private String orderNo;
    private BigDecimal payAmount;
    private BigDecimal TotalAmount;
    private Integer status;

}
