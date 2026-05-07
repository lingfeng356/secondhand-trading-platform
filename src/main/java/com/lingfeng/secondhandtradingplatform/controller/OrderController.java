package com.lingfeng.secondhandtradingplatform.controller;

import com.lingfeng.secondhandtradingplatform.DTO.Result;
import com.lingfeng.secondhandtradingplatform.DTO.request.PageRequest;
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
    @ApiResponses({
            @ApiResponse(responseCode = "400",description = "商品id无效"),
            @ApiResponse(responseCode = "403",description = "无权限"),
            @ApiResponse(responseCode = "404",description = "商品不存在")
    })
    public Result createOrder(@PathVariable String productId){
        return orderService.createOrder(productId);
    }

    //订单详情
    @GetMapping("/detail/{orderId}")
    @Operation(summary = "查询订单详情")
    @ApiResponses({
            @ApiResponse(responseCode = "403",description = "无权限"),
            @ApiResponse(responseCode = "404",description = "卖家不存在")
    })
    public Result orderDetail(@PathVariable String orderId){
        return orderService.orderDetail(orderId);
    }

    //取消订单
    @PostMapping("/cancel/{orderId}")
    @Operation(summary = "取消订单")
    @ApiResponses({
            @ApiResponse(responseCode = "400",description = "商品id无效"),
            @ApiResponse(responseCode = "403",description = "无权限"),
            @ApiResponse(responseCode = "404",description = "商品不存在")
    })
    public Result cancelOrder(@PathVariable String orderId){
        return orderService.cancelOrder(orderId);
    }

    //支付订单
    @PostMapping("/pay/{orderId}")
    @Operation(summary = "支付订单")
    @ApiResponses({
            @ApiResponse(responseCode = "400",description = "商品id无效"),
            @ApiResponse(responseCode = "403",description = "无权限"),
            @ApiResponse(responseCode = "404",description = "商品不存在")
    })
    public Result payOrder(@PathVariable String orderId){
        return orderService.payOrder(orderId);
    }

    //订单退款
    @PostMapping("/refund/{orderId}")
    @Operation(summary = "订单退款")
    @ApiResponses({
            @ApiResponse(responseCode = "400",description = "商品id无效"),
            @ApiResponse(responseCode = "403",description = "无权限"),
            @ApiResponse(responseCode = "404",description = "商品不存在")
    })
    public Result refundOrder(@PathVariable String orderId){
        return orderService.refundOrder(orderId);
    }

    //卖家发货
    @PostMapping("/ship/{orderId}")
    @Operation(summary = "卖家发货")
    @ApiResponses({
            @ApiResponse(responseCode = "400",description = "商品id无效"),
            @ApiResponse(responseCode = "403",description = "无权限"),
            @ApiResponse(responseCode = "404",description = "商品不存在")
    })
    public Result shipOrder(@PathVariable String orderId){
        return orderService.shipOrder(orderId);
    }

    //确认收货
    @PostMapping("/received/{orderId}")
    @Operation(summary = "确认收货")
    @ApiResponses({
            @ApiResponse(responseCode = "400",description = "商品id无效"),
            @ApiResponse(responseCode = "403",description = "无权限"),
            @ApiResponse(responseCode = "404",description = "商品不存在")
    })
    public Result receivedOrder(@PathVariable String orderId){
        return orderService.receivedOrder(orderId);
    }

    //订单列表
    @PostMapping("/myOrderList")
    @Operation(summary = "查询我的订单列表")
    public Result orderList(@RequestBody PageRequest pageRequest){
        return orderService.orderList(pageRequest);
    }

    //查询买到的商品订单
    @PostMapping("/myBoughtList")
    @Operation(summary = "查询我买到的订单")
    public Result myBoughtList(@RequestBody PageRequest pageRequest){
        return orderService.myBoughtList(pageRequest);
    }

    //查询卖出的商品订单
    @PostMapping("/mySoldList")
    @Operation(summary = "查询我卖出的订单")
    public Result mySoldList(@RequestBody PageRequest pageRequest){
        return orderService.mySoldList(pageRequest);
    }

    //删除订单
    @PostMapping("/deleteOrder/{orderId}")
    @Operation(summary = "删除订单")
    @ApiResponses({
            @ApiResponse(responseCode = "404",description = "订单不存在"),
            @ApiResponse(responseCode = "400",description = "订单当前状态无法删除"),
            @ApiResponse(responseCode = "403",description = "只能删除自己的订单")
    })
    public Result deleteOrder(@PathVariable String orderId){
        return orderService.deleteOrder(orderId);
    }
}
