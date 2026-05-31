package com.lingfeng.secondhandtradingplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingfeng.secondhandtradingplatform.pojo.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    @Select("select * from t_order_item where order_id = #{orderId}")
    List<OrderItem> selectByOrderId(String orderId);

    @Select("select count(*) from order_item oi inner join `order` o  on oi.order_id = o.id " +
            "where o.user_id = #{userId} and oi.product_id = #{productId} and o.status = 4")
    Integer checkUserPurchased(@Param("userId") Long userId,
                               @Param("productId") Long productId);
}
