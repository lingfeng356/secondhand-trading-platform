package com.lingfeng.secondhandtradingplatform.service;


import cn.hutool.db.Page;
import com.lingfeng.secondhandtradingplatform.DTO.Result;
import com.lingfeng.secondhandtradingplatform.DTO.request.PageRequest;

public interface OrderService {


    Result createOrder(String product);

    Result orderDetail(String orderId);

    Result cancelOrder(String orderId);

    Result payOrder(String orderId);

    Result refundOrder(String orderId);

    Result receivedOrder(String orderId);

    Result orderList(PageRequest pageRequest);

    Result shipOrder(String orderId);

    Result myBoughtList(PageRequest pageRequest);

    Result mySoldList(PageRequest pageRequest);

    Result deleteOrder(String orderId);
}
