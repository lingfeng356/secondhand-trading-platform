package com.lingfeng.secondhandtradingplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingfeng.secondhandtradingplatform.DTO.response.CreateOrderResponse;
import com.lingfeng.secondhandtradingplatform.pojo.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

}
