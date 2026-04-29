package com.lingfeng.secondhandtradingplatform.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
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

    //| 1-待付款 | 2-待发货 | 3-待收货 | 4-已完成 | 5-已取消 | 6-已退款 |
    private Integer status;

    private Integer payType;

    private LocalDateTime payTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}