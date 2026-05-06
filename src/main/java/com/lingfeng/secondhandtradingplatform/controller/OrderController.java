package com.lingfeng.secondhandtradingplatform.controller;

import com.lingfeng.secondhandtradingplatform.DTO.Result;
import com.lingfeng.secondhandtradingplatform.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
@Tag(name = "订单模块")
public class OrderController {

    @Autowired
    private OrderService orderService;

    //创建订单
    @PostMapping("/create/{productId}")
    @Operation(summary = "创建订单")
    public Result createOrder(@PathVariable String productId){
        return orderService.createOrder(productId);
    }

    //订单详情
    @GetMapping("/detail/{orderId}")
    @Operation(summary = "查询订单详情")
    public Result orderDetail(@RequestHeader("token") String token,@PathVariable String orderId){
        return orderService.orderDetail(token,orderId);
    }

    //取消订单
    @PostMapping("/cancel/{orderId}")
    @Operation(summary = "取消订单")
    public Result cancelOrder(@RequestHeader("token") String token,@PathVariable String orderId){
        return orderService.cancelOrder(token,orderId);
    }

    //支付订单
    @PostMapping("/pay/{orderId}")
    @Operation(summary = "支付订单")
    public Result payOrder(@RequestHeader("token") String token,@PathVariable String orderId){
        return orderService.payOrder(token,orderId);
    }

    //订单退款
    @PostMapping("/refund/{orderId}")
    @Operation(summary = "订单退款")
    public Result refundOrder(@RequestHeader("token") String token,@PathVariable String orderId){
        return orderService.refundOrder(token,orderId);
    }

    //卖家发货
    @PostMapping("/ship/{orderId}")
    @Operation(summary = "卖家发货")
    public Result shipOrder(@RequestHeader("token") String token,@PathVariable String orderId){
        return orderService.shipOrder(token,orderId);
    }

    //确认收货
    @PostMapping("/received/{orderId}")
    @Operation(summary = "确认收货")
    public Result receivedOrder(@RequestHeader("token") String token,@PathVariable String orderId){
        return orderService.receivedOrder(token,orderId);
    }

    //订单列表
    @GetMapping("/myOrderList")
    @Operation(summary = "查询我的订单列表")
    public Result orderList(@RequestHeader("token") String token,
                            @RequestParam(defaultValue = "1") Integer pageNum,
                            @RequestParam(defaultValue = "10") Integer pageSize){
        return orderService.orderList(token,pageNum,pageSize);
    }

    //查询买到的商品订单
    @GetMapping("/myBoughtList")
    @Operation(summary = "查询我买到的订单")
    public Result myBoughtList(@RequestHeader("token") String token,
                               @RequestParam(defaultValue = "1") Integer pageNum,
                               @RequestParam(defaultValue = "10") Integer pageSize){
        return orderService.myBoughtList(token,pageNum,pageSize);
    }

    //查询卖出的商品订单
    @GetMapping("/mySoldList")
    @Operation(summary = "查询我卖出的订单")
    public Result mySoldList(@RequestHeader("token") String token,
                               @RequestParam(defaultValue = "1") Integer pageNum,
                               @RequestParam(defaultValue = "10") Integer pageSize){
        return orderService.mySoldList(token,pageNum,pageSize);
    }

    //删除订单
    @PostMapping("/deleteOrder/{orderId}")
    @Operation(summary = "删除订单")
    @ApiResponses({
            @ApiResponse(responseCode = "404",description = "订单不存在"),
            @ApiResponse(responseCode = "400",description = "订单当前状态无法删除"),
            @ApiResponse(responseCode = "403",description = "只能删除自己的订单")
    })
    public Result deleteOrder(@RequestHeader("token") String token,@PathVariable String orderId){
        return orderService.deleteOrder(token,orderId);
    }
}
