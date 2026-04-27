package com.lingfeng.secondhandtradingplatform.service;

import com.lingfeng.secondhandtradingplatform.DTO.Result;
import com.lingfeng.secondhandtradingplatform.pojo.Product;

public interface ProductService {
    Result publishProduct(String token, Product product);

    Result productDetail(String productId);

    Result productUpdate(Product product, String productId,String token);

    Result removeProduct(String productId, String token);

    Result deleteProduct(String productId, String token);

    Result republishProduct(String productId, String token);

    Result showMyList(String token,Integer pageNum,Integer pageSize);
}
