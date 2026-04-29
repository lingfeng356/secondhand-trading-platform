package com.lingfeng.secondhandtradingplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingfeng.secondhandtradingplatform.pojo.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    @Select("select * from t_order_item where order_id = #{orderId}")
    List<OrderItem> selectByOrderId(String orderId);
}
