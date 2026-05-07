package com.lingfeng.secondhandtradingplatform.converter;


import com.lingfeng.secondhandtradingplatform.DTO.response.OrderDetailResponse;
import com.lingfeng.secondhandtradingplatform.pojo.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderConverter {

    Order toEntity(OrderDetailResponse request);

    OrderDetailResponse toOrderDetailResponse(Order order);
}
