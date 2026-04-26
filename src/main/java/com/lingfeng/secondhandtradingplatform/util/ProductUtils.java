package com.lingfeng.secondhandtradingplatform.util;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingfeng.secondhandtradingplatform.DTO.Result;
import com.lingfeng.secondhandtradingplatform.mapper.ProductMapper;
import com.lingfeng.secondhandtradingplatform.pojo.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProductUtils extends ServiceImpl<ProductMapper,Product> {

    //校验商品id是否合法
    public Result checkProductId(String productId){
        //校验id是否合法
        if(productId == null || productId.trim().isEmpty()){
            log.info("商品id不能为空");
            return Result.error("商品id不能为空");
        }

        return null;
    }

    //鉴权token
    public Result checkToken(String productId,String token){
        //鉴权token,判断是否有资格编辑商品信息
        Long userId = UserUtils.getIdByToken(token);
        log.info("正在获取用户id,用户id为{}",userId);
        Product product = getById(productId);
        //判断product是否存在
        if(product == null){
            return Result.error("商品不存在");
        }
        Long realUserId = product.getUserId();
        log.info("正在获取商品用户id,id为{}",realUserId);
        if(!realUserId.equals(userId)){
            log.info("不允许修改他人商品信息");
            return Result.error("只能修改自己的商品");
        }

        return null;
    }

}
