package com.lingfeng.secondhandtradingplatform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingfeng.secondhandtradingplatform.DTO.Result;
import com.lingfeng.secondhandtradingplatform.mapper.ProductMapper;
import com.lingfeng.secondhandtradingplatform.pojo.Product;
import com.lingfeng.secondhandtradingplatform.service.ProductService;
import com.lingfeng.secondhandtradingplatform.util.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.lingfeng.secondhandtradingplatform.constant.RedisConstant.PRODUCT_KEY;
import static com.lingfeng.secondhandtradingplatform.constant.RedisConstant.PRODUCT_TTL;

@Slf4j
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper,Product> implements ProductService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result publishProduct(String token, Product product) {
        //token校验
        Long userId = UserUtils.getIdByToken(token);
        log.info("查询到用户id为{}",userId);
        //userid在过滤器中判断是否为null
        //商品参数校验在controller类和product类中注解进行校验
        //存数据库
        product.setUserId(userId);
        log.info("保存用户id为{}",userId);
        save(product);
        log.info("已保存到数据库");
        //Mybatis-plus保存完数据会自动将数据赋值给对象
        Long productId = product.getId();
        //缓存redis
        String key = PRODUCT_KEY + productId;
        Map<String,Object> tempMap = BeanUtil.beanToMap(product);
        Map<String,String> productMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : tempMap.entrySet()) {
            if (entry.getValue() != null) {
                productMap.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        stringRedisTemplate.opsForHash().putAll(key,productMap);
        stringRedisTemplate.expire(key,PRODUCT_TTL, TimeUnit.MINUTES);
        log.info("已保存到redis");
        //返回结果

        return Result.success();
    }
}
