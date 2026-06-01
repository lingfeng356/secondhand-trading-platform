package com.lingfeng.secondhandtradingplatform.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@TableName("t_order")
public class Order {

    @TableId(type = IdType.ASSIGN_UUID)
    private String orderId;

    private Long userId;

    private Long sellerId;

    private String orderNo;

    private BigDecimal totalAmount;

    private BigDecimal discountAmount;

    private BigDecimal payAmount;

        //| 0-待付款 | 1-待发货 | 2-待收货 | 3-已完成 | 4-已取消 | 5-已退款 |
    private Integer status;

    private Integer payType;

    private LocalDateTime payTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField(exist = false)
    private List<OrderItem> orderItems;
}