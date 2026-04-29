package com.lingfeng.secondhandtradingplatform.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("t_order_item")
public class OrderItem {

    @TableId(type = IdType.ASSIGN_UUID)
    private String itemId;

    private String orderId;

    private String productId;

    private String productName;

    private String productImage;

    private BigDecimal price;

    private Integer quantity;

    private BigDecimal amount;
}