package com.lingfeng.secondhandtradingplatform.service;


import com.lingfeng.secondhandtradingplatform.DTO.Result;

public interface OrderService {


    Result createOrder(String token, String product);

    Result orderDetail(String token, String orderId);

    Result cancelOrder(String token, String orderId);

    Result payOrder(String token, String orderId);

    Result refundOrder(String token, String orderId);

    Result receivedOrder(String token, String orderId);

    Result orderList(String token,Integer pageNum,Integer pageSize);

    Result shipOrder(String token, String orderId);

    Result myBoughtList(String token, Integer pageNum, Integer pageSize);

    Result mySoldList(String token, Integer pageNum, Integer pageSize);

    Result deleteOrder(String token, String orderId);
}
