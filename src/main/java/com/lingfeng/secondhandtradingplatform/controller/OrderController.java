package com.lingfeng.secondhandtradingplatform.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lingfeng.secondhandtradingplatform.DTO.Result;
import com.lingfeng.secondhandtradingplatform.DTO.request.GetMyListByStatusRequest;
import com.lingfeng.secondhandtradingplatform.DTO.request.OrderCreateRequest;
import com.lingfeng.secondhandtradingplatform.DTO.request.PageRequest;
import com.lingfeng.secondhandtradingplatform.DTO.response.OrderDetailResponse;
import com.lingfeng.secondhandtradingplatform.pojo.Order;
import com.lingfeng.secondhandtradingplatform.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
@Tag(name = "订单模块")
@Validated  // 加在类上，类中所有方法参数都会自动校验
public class OrderController {

    @Autowired
    private OrderService orderService;

    //创建订单
    @PostMapping("/create")
    @Operation(summary = "创建订单")
    @ApiResponses({
            @ApiResponse(responseCode = "400",description = "商品id无效"),
            @ApiResponse(responseCode = "403",description = "无权限"),
            @ApiResponse(responseCode = "404",description = "商品不存在")
    })
    @SaCheckLogin
    public Result<Void> createOrder(@Valid @RequestBody OrderCreateRequest request){
        Long userId = StpUtil.getLoginIdAsLong();
        Long productId = request.getProductId();
        Integer quantity = request.getQuantity();
        return orderService.createOrder(userId,productId,quantity);
    }

    //订单详情
    @GetMapping("/detail/{orderId}")
    @Operation(summary = "查询订单详情")
    @ApiResponses({
            @ApiResponse(responseCode = "403",description = "无权限"),
            @ApiResponse(responseCode = "404",description = "卖家不存在")
    })
    @SaCheckLogin
    public Result<OrderDetailResponse> orderDetail(@PathVariable String orderId){
        Long userId = StpUtil.getLoginIdAsLong();
        return orderService.orderDetail(userId,orderId);
    }

    //取消订单
    @PostMapping("/cancel/{orderId}")
    @Operation(summary = "取消订单")
    @ApiResponses({
            @ApiResponse(responseCode = "400",description = "商品id无效"),
            @ApiResponse(responseCode = "403",description = "无权限"),
            @ApiResponse(responseCode = "404",description = "商品不存在")
    })
    @SaCheckLogin
    public Result<Void> cancelOrder(@PathVariable String orderId){
        Long userId = StpUtil.getLoginIdAsLong();
        return orderService.cancelOrder(userId,orderId);
    }

    //支付订单
    @PostMapping("/pay/{orderId}")
    @Operation(summary = "支付订单")
    @ApiResponses({
            @ApiResponse(responseCode = "400",description = "商品id无效"),
            @ApiResponse(responseCode = "403",description = "无权限"),
            @ApiResponse(responseCode = "404",description = "商品不存在")
    })
    @SaCheckLogin
    public Result<Void> payOrder(@PathVariable String orderId){
        Long userId = StpUtil.getLoginIdAsLong();
        return orderService.payOrder(userId,orderId);
    }

    //订单退款
    @PostMapping("/refund/{orderId}")
    @Operation(summary = "订单退款")
    @ApiResponses({
            @ApiResponse(responseCode = "400",description = "商品id无效"),
            @ApiResponse(responseCode = "403",description = "无权限"),
            @ApiResponse(responseCode = "404",description = "商品不存在")
    })
    @SaCheckLogin
    public Result<Void> refundOrder(@PathVariable String orderId){
        Long userId = StpUtil.getLoginIdAsLong();
        return orderService.refundOrder(userId,orderId);
    }

    //卖家发货
    @PostMapping("/ship/{orderId}")
    @Operation(summary = "卖家发货")
    @ApiResponses({
            @ApiResponse(responseCode = "400",description = "商品id无效"),
            @ApiResponse(responseCode = "403",description = "无权限"),
            @ApiResponse(responseCode = "404",description = "商品不存在")
    })
    @SaCheckLogin
    public Result<Void> shipOrder(@PathVariable String orderId){
        Long userId = StpUtil.getLoginIdAsLong();
        return orderService.shipOrder(userId,orderId);
    }

    //确认收货
    @PostMapping("/received/{orderId}")
    @Operation(summary = "确认收货")
    @ApiResponses({
            @ApiResponse(responseCode = "400",description = "商品id无效"),
            @ApiResponse(responseCode = "403",description = "无权限"),
            @ApiResponse(responseCode = "404",description = "商品不存在")
    })
    @SaCheckLogin
    public Result<Void> receivedOrder(@PathVariable String orderId){
        Long userId = StpUtil.getLoginIdAsLong();
        return orderService.receivedOrder(userId,orderId);
    }

    //订单列表
    @PostMapping("/myOrderList")
    @Operation(summary = "查询我的订单列表")
    @SaCheckLogin
    public Result<IPage<Order>> orderList(@Valid @RequestBody PageRequest pageRequest){
        Long userId = StpUtil.getLoginIdAsLong();
        return orderService.orderList(userId,pageRequest);
    }

    //查询买到的商品订单
    @PostMapping("/myBoughtList")
    @Operation(summary = "查询我买到的订单")
    @SaCheckLogin
    public Result<IPage<Order>> myBoughtList(@Valid @RequestBody PageRequest pageRequest){
        Long userId = StpUtil.getLoginIdAsLong();
        return orderService.myBoughtList(userId,pageRequest);
    }

    //查询卖出的商品订单
    @PostMapping("/mySoldList")
    @Operation(summary = "查询我卖出的订单")
    @SaCheckLogin
    public Result<IPage<Order>> mySoldList(@Valid @RequestBody PageRequest pageRequest){
        Long userId = StpUtil.getLoginIdAsLong();
        return orderService.mySoldList(userId,pageRequest);
    }

    //删除订单
    @PostMapping("/deleteOrder/{orderId}")
    @Operation(summary = "删除订单")
    @ApiResponses({
            @ApiResponse(responseCode = "404",description = "订单不存在"),
            @ApiResponse(responseCode = "400",description = "订单当前状态无法删除"),
            @ApiResponse(responseCode = "403",description = "只能删除自己的订单")
    })
    @SaCheckLogin
    public Result<Void> deleteOrder(@PathVariable String orderId){
        Long userId = StpUtil.getLoginIdAsLong();
        return orderService.deleteOrder(userId,orderId);
    }

    //我买到的状态筛选
    @PostMapping("/myBoughtListByStatus")
    @Operation(summary = "查询我买到的订单")
    @SaCheckLogin
    public Result<IPage<Order>> myBoughtListByStatus(@Valid @RequestBody GetMyListByStatusRequest request){
        Long userId = StpUtil.getLoginIdAsLong();
        return orderService.myBoughtListByStatus(userId,request);
    }

    //我卖出的状态筛选
    @PostMapping("/mySoldListByStatus")
    @Operation(summary = "查询我卖出的订单")
    @SaCheckLogin
    public Result<IPage<Order>> mySoldListByStatus(@Valid @RequestBody GetMyListByStatusRequest request){
        Long userId = StpUtil.getLoginIdAsLong();
        return orderService.mySoldListByStatus(userId,request);
    }
}
