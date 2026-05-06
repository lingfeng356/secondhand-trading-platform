package com.lingfeng.secondhandtradingplatform.converter;


import com.lingfeng.secondhandtradingplatform.DTO.response.CreateOrderResponse;
import com.lingfeng.secondhandtradingplatform.pojo.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderConverter {

    Order toEntity(CreateOrderResponse request);

    CreateOrderResponse toCreateOrderRequest(Order order);
}
