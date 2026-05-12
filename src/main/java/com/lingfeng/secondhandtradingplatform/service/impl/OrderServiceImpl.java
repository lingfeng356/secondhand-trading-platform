package com.lingfeng.secondhandtradingplatform.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingfeng.secondhandtradingplatform.DTO.Result;
import com.lingfeng.secondhandtradingplatform.DTO.request.GetMyListByStatusRequest;
import com.lingfeng.secondhandtradingplatform.DTO.request.PageRequest;
import com.lingfeng.secondhandtradingplatform.DTO.response.OrderDetailResponse;
import com.lingfeng.secondhandtradingplatform.converter.OrderConverter;
import com.lingfeng.secondhandtradingplatform.mapper.OrderItemMapper;
import com.lingfeng.secondhandtradingplatform.mapper.OrderMapper;
import com.lingfeng.secondhandtradingplatform.mapper.ProductMapper;
import com.lingfeng.secondhandtradingplatform.mapper.UserMapper;
import com.lingfeng.secondhandtradingplatform.pojo.Order;
import com.lingfeng.secondhandtradingplatform.pojo.OrderItem;
import com.lingfeng.secondhandtradingplatform.pojo.Product;
import com.lingfeng.secondhandtradingplatform.pojo.User;
import com.lingfeng.secondhandtradingplatform.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.lingfeng.secondhandtradingplatform.constant.RedisConstant.*;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private OrderConverter orderConverter;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private OrderMapper orderMapper;

    //创建订单 TODO:目前做的是买单个商品，待做一次购买多个商品
    @Override
    public Result<Void> createOrder(Long userId, Long productId) {

        log.info("创建订单请求:userId={},productId={}",userId,productId);

        //校验id是否合法
        if(productId == null || productId <= 0){
            log.warn("创建订单失败:商品id无效,userId={},productId={}",userId,productId);
            return Result.error(400,"商品id无效");
        }

        Product product = productMapper.selectById(productId);

        //查询商品是否存在
        if(product == null){
            log.warn("创建订单失败:商品不存在,userId={},productId={}",userId,productId);
            return Result.error(404,"商品不存在");
        }

        //校验商品状态
        Integer status = product.getStatus();
        if(status != 1){
            log.info("创建订单失败:商品状态无效,userId={},productId={}",userId,productId);
            return Result.error(400,"商品状态无效");
        }

        //鉴权
        if(userId.equals(product.getUserId())){
            log.warn("创建订单失败:无权限,userId={},productId={}",userId,productId);
            return Result.error(403,"无权限");
        }

        //计算订单金额
        BigDecimal price = product.getPrice();

        //生成订单编号(时间戳加随机数)
        String orderNo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
                + RandomUtil.randomNumbers(4);

        //保存订单到数据库
        Order order = new Order();
        order.setUserId(userId);
        order.setSellerId(product.getUserId());
        order.setOrderNo(orderNo);
        order.setPayAmount(price);
        order.setTotalAmount(price);
        order.setStatus(1);
        save(order);

        //保存订单商品表
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(order.getOrderId());
        orderItem.setProductId(productId);
        orderItem.setProductName(product.getTitle());
        orderItem.setProductImage(product.getImages());
        orderItem.setAmount(order.getPayAmount());
        orderItem.setPrice(order.getPayAmount());
        orderItem.setQuantity(1);
        orderItemMapper.insert(orderItem);

        //更新商品状态
        product.setStatus(2);
        productMapper.updateById(product);
        stringRedisTemplate.delete(PRODUCT_KEY + productId);

        //返回信息
        String orderId = order.getOrderId();
        log.info("创建订单成功:userId={},productId={},orderId={}",userId,productId,orderId);
        return Result.success();
    }

    //查看订单详情
    @Override
    public Result<OrderDetailResponse> orderDetail(Long userId, String orderId) {

        log.info("查看订单请求:userId={},orderId={}",userId,orderId);

        //检查订单是否存在
        Order order = getById(orderId);
        if (order == null) {
            log.warn("查看订单失败:订单不存在,userId={},orderId={}",userId,orderId);
            return Result.error(404,"订单不存在");
        }

        //校验订单是否为该用户的订单
        Long rUserId = order.getUserId();
        Long rSellerId = order.getSellerId();
        if(!rUserId.equals(userId) && !rSellerId.equals(userId)){
            log.warn("查看订单失败:无权限,userId={},orderId={}",userId,orderId);
            return Result.error(403,"无权限");
        }

        //查询订单商品信息
        OrderDetailResponse response = orderConverter.toOrderDetailResponse(order);
        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);

        //卖家判空
        User seller = userMapper.selectById(rSellerId);
        if(seller == null){
            log.warn("查询订单失败:卖家不存在,userId={},orderId={}",userId,orderId);
            return Result.error(404,"卖家不存在");
        }

        //将订单和商品信息存入响应
        response.setItems(items);
        response.setSellerId(seller.getId());
        response.setSellerImg(seller.getImg());
        response.setSellerName(seller.getUsername());

        //返回信息
        log.info("查询订单成功:userId={},orderId={}",userId,orderId);
        return Result.success(response);
    }

    //取消订单
    @Override
    public Result<Void> cancelOrder(Long userId, String orderId) {

        log.info("取消订单请求:userId={},orderId={}",userId,orderId);

        //检查订单是否存在
        Order order = getById(orderId);
        if (order == null) {
            log.warn("取消订单失败:userId={},orderId={}",userId,orderId);
            return Result.error(404,"订单不存在");
        }

        //校验订单状态是否能够合法操作
        Integer status = order.getStatus();
        if(status == 3 || status == 4 || status == 5 || status == 6){
            log.warn("取消订单失败:当前订单状态无法取消,userId={},orderId={}",userId,orderId);
            return Result.error(400,"当前订单状态无法取消");
        }

        //校验订单是否为该用户的订单
        Long rUserId = order.getUserId();
        if(!rUserId.equals(userId)){
            log.warn("取消订单失败:无权限,userId={},orderId={}",userId,orderId);
            return Result.error(403,"无权限");
        }

        //修改订单状态
        order.setStatus(5);

        //商品状态恢复
        List<OrderItem> orderItem = orderItemMapper.selectByOrderId(orderId);
        for(OrderItem oi:orderItem){
            Long productId = oi.getProductId();
            Product product = productMapper.selectById(productId);
            product.setStatus(1);
            //更新数据库
            productMapper.updateById(product);
            //删除redis缓存
            stringRedisTemplate.delete(PRODUCT_KEY + productId);
        }

        //更新数据库
        updateById(order);

        //返回结果
        log.info("取消订单成功:userId={},orderId={}",userId,orderId);
        return Result.success();
    }

    //支付订单
    @Override
    public Result<Void> payOrder(Long userId, String orderId) {

        log.info("支付订单请求:userId={},orderId={}",userId,orderId);

        //检查订单是否存在
        Order order = getById(orderId);
        if (order == null) {
            log.warn("支付订单失败:userId={},orderId={}",userId,orderId);
            return Result.error(404,"订单不存在");
        }

        //校验订单状态是否能够合法操作
        Integer status = order.getStatus();
        if(status != 1){
            log.warn("支付订单失败:当前订单状态无法支付,userId={},orderId={}",userId,orderId);
            return Result.error(400,"当前订单状态无法支付");
        }

        //校验订单是否为该用户的订单
        Long rUserId = order.getUserId();
        if(!rUserId.equals(userId)){
            log.warn("支付订单失败:无权限,userId={},orderId={}",userId,orderId);
            return Result.error(403,"无权限");
        }

        //支付订单
        //更改订单状态
        order.setStatus(2);
        order.setPayTime(LocalDateTime.now());

        //默认支付方式为微信支付
        order.setPayType(1);

        //更新数据库
        updateById(order);

        //返回成功信息
        log.info("支付订单成功:userId={},orderId={}",userId,orderId);
        return Result.success();
    }

    //退款订单
    @Override
    public Result<Void> refundOrder(Long userId, String orderId) {

        log.info("退款订单请求:userId={},orderId={}",userId,orderId);

        //检查订单是否存在
        Order order = getById(orderId);
        if (order == null) {
            log.warn("退款订单失败:userId={},orderId={}", userId, orderId);
            return Result.error(404, "订单不存在");
        }

        //校验订单状态是否能够合法操作
        Integer status = order.getStatus();
        if(status != 2 && status != 3){
            log.warn("退款订单失败:当前订单状态无法退款,userId={},orderId={}",userId,orderId);
            return Result.error(400,"当前订单状态无法退款");
        }

        //校验订单是否为该用户的订单
        Long rUserId = order.getUserId();
        if(!rUserId.equals(userId)){
            log.warn("退款订单失败:无权限,userId={},orderId={}",userId,orderId);
            return Result.error(403,"无权限");
        }

        //修改订单状态
        order.setStatus(6);

        //商品状态恢复
        List<OrderItem> orderItem = orderItemMapper.selectByOrderId(orderId);
        for(OrderItem oi:orderItem){
            Long productId = oi.getProductId();
            Product product = productMapper.selectById(productId);
            product.setStatus(1);
            //更新数据库
            productMapper.updateById(product);
            //删除redis缓存
            stringRedisTemplate.delete(PRODUCT_KEY + productId);
        }

        //更新数据库
        updateById(order);

        //返回成功信息
        log.info("退款订单成功:userId={},orderId={}",userId,orderId);
        return Result.success();
    }

    //确认收货
    @Override
    public Result<Void> receivedOrder(Long userId, String orderId) {

        log.info("确认收货请求:userId={},orderId={}",userId,orderId);

        //检查订单是否存在
        Order order = getById(orderId);
        if (order == null) {
            log.warn("确认收货失败:userId={},orderId={}", userId, orderId);
            return Result.error(404, "订单不存在");
        }

        //校验订单状态是否能够合法操作
        Integer status = order.getStatus();
        if(status != 3){
            log.warn("确认收货失败:当前订单状态无法收货,userId={},orderId={}",userId,orderId);
            return Result.error(400,"当前订单状态无法收货");
        }

        //校验订单是否为该用户的订单
        Long rUserId = order.getUserId();
        if(!rUserId.equals(userId)){
            log.warn("确认收货失败:无权限,userId={},orderId={}",userId,orderId);
            return Result.error(403,"无权限");
        }

        //修改订单状态
        order.setStatus(4);

        //更新数据库
        updateById(order);

        //返回成功信息
        log.info("确认收货成功:userId={},orderId={}",userId,orderId);
        return Result.success();
    }

    //卖家发货
    @Override
    public Result<Void> shipOrder(Long userId, String orderId) {

        log.info("卖家发货请求:userId={},orderId={}",userId,orderId);

        //检查订单是否存在
        Order order = getById(orderId);
        if (order == null) {
            log.warn("卖家发货失败:userId={},orderId={}", userId, orderId);
            return Result.error(404, "订单不存在");
        }

        //校验订单状态是否能够合法操作
        Integer status = order.getStatus();
        if(status != 2){
            log.warn("卖家发货失败:当前订单状态无法发货,userId={},orderId={}",userId,orderId);
            return Result.error(400,"当前订单状态无法发货");
        }

        //校验订单是否为该用户的订单
        Long rSellerId = order.getSellerId();
        if(!rSellerId.equals(userId)){
            log.warn("卖家发货失败:无权限,userId={},orderId={}",userId,orderId);
            return Result.error(403,"无权限");
        }

        //修改订单状态
        order.setStatus(3);

        //更新数据库
        updateById(order);

        //返回成功信息
        log.info("卖家发货成功:userId={},orderId={}",userId,orderId);
        return Result.success();
    }

    //订单列表展示
    @Override
    public Result<IPage<Order>> orderList(Long userId, PageRequest pageRequest) {

        log.info("我的订单查询请求:userId={}",userId);

        Integer pageNum = pageRequest.getPageNum();
        Integer pageSize = pageRequest.getPageSize();

        //创建查询条件
        Page<Order> page = new Page<>(pageNum,pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId)
                .or()
                .eq(Order::getSellerId, userId);
        wrapper.orderByDesc(Order::getCreateTime);  // 单独一行，不要链上去

        //开始查询
        page(page,wrapper);

        //查询买到的订单商品
        //TODO：可以优化成一次联表查询，效率提升十倍
        for(Order order:page.getRecords()){
            List<OrderItem> orderItems = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId,order.getOrderId())
            );
            order.setOrderItems(orderItems);
        }

        //返回page
        log.info("我的订单查询成功:userId={}",userId);
        return Result.success(page);
    }

    //我买到的商品全部列表
    @Override
    public Result<IPage<Order>> myBoughtList(Long userId, PageRequest pageRequest) {

        log.info("获取我买到的请求:userId={}",userId);

        Integer pageNum = pageRequest.getPageNum();
        Integer pageSize = pageRequest.getPageSize();

        //分页查询买到的订单
        Page<Order> page = new Page<>(pageNum,pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId,userId)
                .orderByDesc(Order::getCreateTime);

        //执行分页查询
        page(page,wrapper);

        //查询买到的订单商品
        //TODO：可以优化成一次联表查询，效率提升十倍
        for(Order order:page.getRecords()){
            List<OrderItem> orderItems = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId,order.getOrderId())
            );
            order.setOrderItems(orderItems);
        }

        //返回信息
        log.info("获取我买到的成功:userId={}",userId);
        return Result.success(page);
    }

    //查询我卖出的商品
    @Override
    public Result<IPage<Order>> mySoldList(Long userId, PageRequest pageRequest) {

        log.info("查询我卖出的请求:userId={}",userId);

        Integer pageNum = pageRequest.getPageNum();
        Integer pageSize = pageRequest.getPageSize();

        //分页查询买到的订单
        Page<Order> page = new Page<>(pageNum,pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getSellerId,userId)
                .orderByDesc(Order::getCreateTime);

        //执行分页查询
        page(page,wrapper);

        //查询买到的订单商品
        //TODO：可以优化成一次联表查询，效率提升十倍
        for(Order order:page.getRecords()){
            List<OrderItem> orderItems = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId,order.getOrderId())
            );
            order.setOrderItems(orderItems);
        }

        //返回信息
        log.info("获取我卖出的成功:userId={}",userId);
        return Result.success(page);
    }

    //TODO:改成双方的逻辑删除
    //删除订单
    @Override
    public Result<Void> deleteOrder(Long userId, String orderId) {

        log.info("删除订单请求:userId={},orderId={}",userId,orderId);

        //获取order
        Order order = getById(orderId);
        if(order == null){
            log.warn("删除订单失败:订单不存在,userId={},orderId={}",userId,orderId);
            return Result.error(404,"订单不存在");
        }

        // ✅ 判断是否是自己的订单（买家或卖家都可以删）
        if (!order.getUserId().equals(userId) && !order.getSellerId().equals(userId)) {
            log.warn("删除订单失败: 无权限, userId={}, orderId={}", userId, orderId);
            return Result.error(403, "无权限");
        }

        //获取订单物品list
        List<OrderItem> orderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId,order.getOrderId())
        );

        //判断order是否取消或者完成或者已退款，如果符合，则删除，不符合，报错
        Integer status = order.getStatus();
        if(status != 4 && status != 5 && status != 6){
            log.warn("删除订单失败:订单当前状态无法删除,userId={},orderId={}",userId,orderId);
            return Result.error(400,"订单当前状态无法删除");
        }

        //删除数据库
        orderMapper.deleteById(orderId);
        for(OrderItem item:orderItems){
            String itemId = item.getItemId();
            orderItemMapper.deleteById(itemId);
        }

        //返回结果
        log.info("删除订单成功:userId={},orderId={}",userId,orderId);
        return Result.success();
    }

    //筛选状态获取我买到的订单
    @Override
    public Result<IPage<Order>> myBoughtListByStatus(Long userId, GetMyListByStatusRequest request) {

        log.info("状态筛选查询我买到的请求:userId={}",userId);

        Integer pageNum = request.getPageNum();
        Integer pageSize = request.getPageSize();
        Integer status = request.getStatus();

        //分页状态查询订单
        Page<Order> page = new Page<>(pageNum,pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId,userId)
                .eq(Order::getStatus,status)
                .orderByDesc(Order::getCreateTime);

        //执行分页查询
        page(page,wrapper);

        //查询买到的订单商品
        //TODO：可以优化成一次联表查询，效率提升十倍
        for(Order order:page.getRecords()){
            List<OrderItem> orderItems = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId,order.getOrderId())
            );
            order.setOrderItems(orderItems);
        }

        log.info("状态筛选查询我买到的成功:userId={}",userId);
        return Result.success(page);
    }

    //状态筛选我卖出的订单
    @Override
    public Result<IPage<Order>> mySoldListByStatus(Long userId, GetMyListByStatusRequest request) {

        log.info("状态筛选查询我卖出的请求:userId={}",userId);

        Integer pageNum = request.getPageNum();
        Integer pageSize = request.getPageSize();
        Integer status = request.getStatus();

        //分页状态查询订单
        Page<Order> page = new Page<>(pageNum,pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getSellerId,userId)
                .eq(Order::getStatus,status)
                .orderByDesc(Order::getCreateTime);

        //执行分页查询
        page(page,wrapper);

        //查询买到的订单商品
        //TODO：可以优化成一次联表查询，效率提升十倍
        for(Order order:page.getRecords()){
            List<OrderItem> orderItems = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId,order.getOrderId())
            );
            order.setOrderItems(orderItems);
        }

        log.info("状态筛选查询我卖出的成功:userId={}",userId);
        return Result.success(page);
    }
}
