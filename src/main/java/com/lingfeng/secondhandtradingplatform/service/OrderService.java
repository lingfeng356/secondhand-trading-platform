package com.lingfeng.secondhandtradingplatform.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lingfeng.secondhandtradingplatform.DTO.Result;
import com.lingfeng.secondhandtradingplatform.DTO.request.GetMyListByStatusRequest;
import com.lingfeng.secondhandtradingplatform.DTO.request.PageRequest;
import com.lingfeng.secondhandtradingplatform.DTO.response.OrderDetailResponse;
import com.lingfeng.secondhandtradingplatform.pojo.Order;

public interface OrderService {


    Result<Void> createOrder(Long userId, Long productId);

    Result<OrderDetailResponse> orderDetail(Long userId, String orderId);

    Result<Void> cancelOrder(Long userId, String orderId);

    Result<Void> payOrder(Long userId, String orderId);

    Result<Void> refundOrder(Long userId, String orderId);

    Result<Void> receivedOrder(Long userId, String orderId);

    Result<IPage<Order>> orderList(Long userId, PageRequest pageRequest);

    Result<Void> shipOrder(Long userId, String orderId);

    Result<IPage<Order>> myBoughtList(Long userId, PageRequest pageRequest);

    Result<IPage<Order>> mySoldList(Long userId, PageRequest pageRequest);

    Result<Void> deleteOrder(Long userId, String orderId);

    Result<IPage<Order>> myBoughtListByStatus(Long userId, GetMyListByStatusRequest request);

    Result<IPage<Order>> mySoldListByStatus(Long userId, GetMyListByStatusRequest request);
}
