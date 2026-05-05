package com.lingfeng.secondhandtradingplatform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingfeng.secondhandtradingplatform.DTO.Result;
import com.lingfeng.secondhandtradingplatform.mapper.OrderItemMapper;
import com.lingfeng.secondhandtradingplatform.mapper.OrderMapper;
import com.lingfeng.secondhandtradingplatform.mapper.ProductMapper;
import com.lingfeng.secondhandtradingplatform.mapper.UserMapper;
import com.lingfeng.secondhandtradingplatform.pojo.Order;
import com.lingfeng.secondhandtradingplatform.pojo.OrderItem;
import com.lingfeng.secondhandtradingplatform.pojo.Product;
import com.lingfeng.secondhandtradingplatform.pojo.User;
import com.lingfeng.secondhandtradingplatform.service.OrderService;
import com.lingfeng.secondhandtradingplatform.util.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private OrderMapper orderMapper;

    //创建订单 TODO:目前做的是买单个商品，待做一次购买多个商品
    @Override
    public Result createOrder(String token, String productId) {
        //校验参数
        if(productId == null){
            log.info("校验参数不通过");
            return Result.error("参数错误");
        }
        log.info("校验参数通过");


        Product product = productMapper.selectById(productId);
        Long userId = UserUtils.getIdByToken(token);

        //查询商品是否存在
        if(product == null){
            log.info("商品不存在");
            return Result.error("商品不存在");
        }
        log.info("商品存在");



        //校验商品状态
        Integer status = product.getStatus();
        if(status != 1){
            log.info("商品目前状态无法购买");
            return Result.error("商品未上架或已售出");
        }
        log.info("商品状态为：已上架");

        if(userId == product.getUserId()){
            return Result.error("不能购买自己的商品");
        }


        //计算订单金额
        BigDecimal price = product.getPrice();
        log.info("订单金额为{}",price);

        //生成订单编号(时间戳加随机数)
        String orderNo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
                + RandomUtil.randomNumbers(4);
        log.info("订单编号为{}",orderNo);

        //保存订单到数据库
        Order order = new Order();
        order.setUserId(userId);
        order.setSellerId(product.getUserId());
        order.setOrderNo(orderNo);
        order.setPayAmount(price);
        order.setTotalAmount(price);
        order.setStatus(1);
        save(order);
        log.info("订单保存到数据库成功");

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
        log.info("订单商品保存到数据库成功");

        //更新商品状态
        product.setStatus(2);
        productMapper.updateById(product);
        log.info("商品状态已更新");

        //返回信息
        Map<String,Object> result = new HashMap<>();
        result.put("orderId",order.getOrderId());
        result.put("orderNo",order.getOrderNo());
        result.put("payAmount",order.getPayAmount());

        return Result.success(result);
    }

    //查看订单详情
    @Override
    public Result orderDetail(String token, String orderId) {
        //检查订单是否存在
        Order order = getById(orderId);
        if (order == null) {
            log.info("订单不存在，orderId: {}", orderId);
            return Result.error("订单不存在");
        }

        //校验订单是否为该用户的订单
        Long userId = UserUtils.getIdByToken(token);
        Long rUserId = order.getUserId();
        Long rSellerId = order.getSellerId();
        if(!rUserId.equals(userId) && !rSellerId.equals(userId)){
            log.info("订单用户与登录用户不一致");
            return Result.error("只能查看自己的订单");
        }

        //查询订单商品信息
        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        User seller = userMapper.selectById(rSellerId);
        Map<String,Object> result = new HashMap<>();
        result.put("order",order);
        result.put("items",items);
        result.put("seller",seller);

        //缓存到redis中
        // 订单对象转 Map
        String orderKey = ORDER_KEY + orderId;
        Map<String, Object> tempMap = BeanUtil.beanToMap(order);
        Map<String, String> orderMap = new HashMap<>();

        for (Map.Entry<String, Object> entry : tempMap.entrySet()) {
            if (entry.getValue() != null) {
                orderMap.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }

        stringRedisTemplate.opsForHash().putAll(orderKey,orderMap);
        stringRedisTemplate.expire(orderKey,ORDER_TTL,TimeUnit.MINUTES);

        //返回信息
        return Result.success(result);
    }

    //取消订单
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result cancelOrder(String token, String orderId) {
        //检查订单是否存在
        Order order = getById(orderId);
        if (order == null) {
            log.info("订单不存在，orderId: {}", orderId);
            return Result.error("订单不存在");
        }

        //校验订单状态是否能够合法操作
        Integer status = order.getStatus();
        if(status == 3 || status == 4 || status == 5 || status == 6){
            log.info("当前订单状态无法取消");
            return Result.error("当前订单状态无法取消");
        }

        //校验订单是否为该用户的订单
        Long userId = UserUtils.getIdByToken(token);
        Long rUserId = order.getUserId();
        if(!rUserId.equals(userId)){
            log.info("订单用户与登录用户不一致");
            return Result.error("只能查看自己的订单");
        }

        //修改订单状态
        order.setStatus(5);
        log.info("订单状态已改为已取消");

        //商品状态恢复
        List<OrderItem> orderItem = orderItemMapper.selectByOrderId(orderId);
        for(OrderItem oi:orderItem){
            String productId = oi.getProductId();
            Product product = productMapper.selectById(productId);
            product.setStatus(1);
            //更新数据库
            productMapper.updateById(product);
            //删除redis缓存
            stringRedisTemplate.delete(PRODUCT_KEY + productId);
        }

        //更新数据库
        updateById(order);
        log.info("更新数据库成功");

        //返回结果
        return Result.success();
    }

    //支付订单
    @Override
    public Result payOrder(String token, String orderId) {
        //检查订单是否存在
        Order order = getById(orderId);
        if (order == null) {
            log.info("订单不存在，orderId: {}", orderId);
            return Result.error("订单不存在");
        }

        //校验订单状态是否能够合法操作
        Integer status = order.getStatus();
        if(status != 1){
            log.info("当前订单状态无法支付");
            return Result.error("当前订单状态无法支付");
        }

        //校验订单是否为该用户的订单
        Long userId = UserUtils.getIdByToken(token);
        Long rUserId = order.getUserId();
        if(!rUserId.equals(userId)){
            log.info("订单用户与登录用户不一致");
            return Result.error("只能查看自己的订单");
        }

        //支付订单
        //更改订单状态
        order.setStatus(2);
        order.setPayTime(LocalDateTime.now());

        //默认支付方式为微信支付
        order.setPayType(1);
        log.info("已将订单状态更改成待发货");

        //更新数据库
        updateById(order);
        log.info("更新数据库成功");

        //删除redis缓存
        stringRedisTemplate.delete(ORDER_KEY + orderId);
        log.info("删除redis缓存成功");

        //返回成功信息
        log.info("支付成功");
        return Result.success();
    }

    //退款订单
    @Override
    public Result refundOrder(String token, String orderId) {
        //检查订单是否存在
        Order order = getById(orderId);
        if (order == null) {
            log.info("订单不存在，orderId: {}", orderId);
            return Result.error("订单不存在");
        }

        //校验订单状态是否能够合法操作
        Integer status = order.getStatus();
        if(status != 2 && status != 3){
            log.info("当前订单状态无法退款");
            return Result.error("当前订单状态无法退款");
        }

        //校验订单是否为该用户的订单
        Long userId = UserUtils.getIdByToken(token);
        Long rUserId = order.getUserId();
        if(!rUserId.equals(userId)){
            log.info("订单用户与登录用户不一致");
            return Result.error("只能查看自己的订单");
        }

        //修改订单状态
        order.setStatus(6);
        log.info("已成功修改订单状态");

        //商品状态恢复
        List<OrderItem> orderItem = orderItemMapper.selectByOrderId(orderId);
        for(OrderItem oi:orderItem){
            String productId = oi.getProductId();
            Product product = productMapper.selectById(productId);
            product.setStatus(1);
            //更新数据库
            productMapper.updateById(product);
            //删除redis缓存
            stringRedisTemplate.delete(PRODUCT_KEY + productId);
        }


        //更新数据库
        updateById(order);
        log.info("已成功更新数据库");

        //删除redis缓存
        stringRedisTemplate.delete(ORDER_KEY + orderId);
        log.info("成功删除redis缓存");

        //此处省略一百万行退款代码
        log.info("退款成功");

        //返回成功信息
        log.info("退款成功");
        return Result.success();
    }

    //确认收货
    @Override
    public Result receivedOrder(String token, String orderId) {
        //检查订单是否存在
        Order order = getById(orderId);
        if (order == null) {
            log.info("订单不存在，orderId: {}", orderId);
            return Result.error("订单不存在");
        }

        //校验订单状态是否能够合法操作
        Integer status = order.getStatus();
        if(status != 3){
            log.info("当前订单状态无法确认收货");
            return Result.error("当前订单状态无法确认收货");
        }

        //校验订单是否为该用户的订单
        Long userId = UserUtils.getIdByToken(token);
        Long rUserId = order.getUserId();
        if(!rUserId.equals(userId)){
            log.info("订单用户与登录用户不一致");
            return Result.error("只能查看自己的订单");
        }

        //修改订单状态
        order.setStatus(4);
        log.info("收货成功");

        //更新数据库
        updateById(order);
        log.info("修改数据库成功");

        //删除redis缓存
        stringRedisTemplate.delete(ORDER_KEY + orderId);
        log.info("删除redis缓存成功");

        //返回成功信息
        log.info("收货成功");
        return Result.success();
    }

    //卖家发货
    @Override
    public Result shipOrder(String token, String orderId) {
        //检查订单是否存在
        Order order = getById(orderId);
        if (order == null) {
            log.info("订单不存在，orderId: {}", orderId);
            return Result.error("订单不存在");
        }

        //校验订单状态是否能够合法操作
        Integer status = order.getStatus();
        if(status != 2){
            log.info("当前订单状态无法发货");
            return Result.error("当前订单状态无法确认发货");
        }

        //校验订单是否为该用户的订单
        Long userId = UserUtils.getIdByToken(token);
        Long rSellerId = order.getSellerId();
        if(!rSellerId.equals(userId)){
            log.info("订单用户与登录用户不一致");
            return Result.error("只能查看自己的订单");
        }

        //修改订单状态
        order.setStatus(3);
        log.info("发货成功");

        //更新数据库
        updateById(order);
        log.info("修改数据库成功");

        //删除redis缓存
        stringRedisTemplate.delete(ORDER_KEY + orderId);
        log.info("删除redis缓存成功");

        //返回成功信息
        log.info("发货成功");
        return Result.success();
    }

    //订单列表展示
    @Override
    public Result orderList(String token,Integer pageNum,Integer pageSize) {
        //获取userId
        Long userId = UserUtils.getIdByToken(token);

        //创建查询条件
        Page<Order> page = new Page<>(pageNum,pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId)
                .or()
                .eq(Order::getSellerId, userId);
        wrapper.orderByDesc(Order::getCreateTime);  // 单独一行，不要链上去

        //开始查询
        page(page,wrapper);

        //返回page
        log.info("成功查询订单列表");

        return Result.success(page);
    }

    //我买到的商品列表
    @Override
    public Result myBoughtList(String token, Integer pageNum, Integer pageSize) {
        //获取userId
        Long userId = UserUtils.getIdByToken(token);

        log.info("获取我买到的请求:userId={}",userId);

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
    public Result mySoldList(String token, Integer pageNum, Integer pageSize) {
        //获取userId
        Long userId = UserUtils.getIdByToken(token);

        log.info("获取我卖出的请求:userId={}",userId);

        //数据库查询是否有该用户
        User user = userMapper.selectById(userId);

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
    public Result deleteOrder(String token, String orderId) {
        //获取userId
        Long userId = UserUtils.getIdByToken(token);

        log.info("删除订单请求:userId={},orderId={}",userId,orderId);

        //获取order
        Order order = getById(orderId);
        if(order == null){
            log.warn("删除订单失败:订单不存在,userId={},orderId={}",userId,orderId);
            return Result.error(404,"订单不存在");
        }

        // ✅ 判断是否是自己的订单（买家或卖家都可以删）
        if (!order.getUserId().equals(userId) && !order.getSellerId().equals(userId)) {
            log.warn("删除订单失败: 无权操作, userId={}, orderId={}", userId, orderId);
            return Result.error(403, "只能删除自己的订单");
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

        //删除redis缓存
        stringRedisTemplate.delete(ORDER_KEY + orderId);

        //返回结果
        log.info("删除订单成功:userId={},orderId={}",userId,orderId);
        return Result.success();
    }
}
