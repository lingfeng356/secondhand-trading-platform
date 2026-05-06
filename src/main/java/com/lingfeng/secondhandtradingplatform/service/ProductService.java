package com.lingfeng.secondhandtradingplatform.service;

import com.lingfeng.secondhandtradingplatform.DTO.request.ProductListRequest;
import com.lingfeng.secondhandtradingplatform.DTO.request.PageRequest;
import com.lingfeng.secondhandtradingplatform.DTO.request.ProductPublishRequest;
import com.lingfeng.secondhandtradingplatform.DTO.Result;
import com.lingfeng.secondhandtradingplatform.DTO.request.UpdateProductDetailRequest;

public interface ProductService {
    Result publishProduct(ProductPublishRequest ppr);

    Result productDetail(String productId);

    Result productUpdate(UpdateProductDetailRequest updr, String productId);

    Result removeProduct(String productId);

    Result deleteProduct(String productId);

    Result republishProduct(String productId);

    Result showMyList(PageRequest pageRequest);

    Result showMyList(ProductListRequest productListRequestDTO);

    Result recommendProducts(PageRequest pageRequest);
}
