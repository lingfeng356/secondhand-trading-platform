package com.lingfeng.secondhandtradingplatform.controller;

import com.lingfeng.secondhandtradingplatform.DTO.Result;
import com.lingfeng.secondhandtradingplatform.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    //创建订单
    @PostMapping("/order/create/{productId}")
    public Result createOrder(@RequestHeader("token") String token, @PathVariable String productId){
        return orderService.createOrder(token,productId);
    }

    //订单详情
    @GetMapping("/order/detail/{orderId}")
    public Result orderDetail(@RequestHeader("token") String token,@PathVariable String orderId){
        return orderService.orderDetail(token,orderId);
    }

    //取消订单
    @PostMapping("/order/cancel/{orderId}")
    public Result cancelOrder(@RequestHeader("token") String token,@PathVariable String orderId){
        return orderService.cancelOrder(token,orderId);
    }

    //支付订单
    @PostMapping("/order/pay/{orderId}")
    public Result payOrder(@RequestHeader("token") String token,@PathVariable String orderId){
        return orderService.payOrder(token,orderId);
    }

    //订单退款
    @PostMapping("/order/refund/{orderId}")
    public Result refundOrder(@RequestHeader("token") String token,@PathVariable String orderId){
        return orderService.refundOrder(token,orderId);
    }

    //卖家发货
    @PostMapping("/order/ship/{orderId}")
    public Result shipOrder(@RequestHeader("token") String token,@PathVariable String orderId){
        return orderService.shipdOrder(token,orderId);
    }

    //确认收货
    @PostMapping("/order/received/{orderId}")
    public Result receivedOrder(@RequestHeader("token") String token,@PathVariable String orderId){
        return orderService.receivedOrder(token,orderId);
    }

    //订单列表
    @GetMapping("/order/myOrderList")
    public Result orderList(@RequestHeader("token") String token,
                            @RequestParam(defaultValue = "1") Integer pageNum,
                            @RequestParam(defaultValue = "10") Integer pageSize){
        return orderService.orderList(token,pageNum,pageSize);
    }
}
