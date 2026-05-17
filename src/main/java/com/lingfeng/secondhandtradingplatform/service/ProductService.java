package com.lingfeng.secondhandtradingplatform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lingfeng.secondhandtradingplatform.DTO.request.*;
import com.lingfeng.secondhandtradingplatform.DTO.Result;
import com.lingfeng.secondhandtradingplatform.DTO.response.ProductDetailResponse;
import com.lingfeng.secondhandtradingplatform.pojo.Product;
import org.springframework.web.multipart.MultipartFile;

public interface ProductService {
    Result<Void> publishProduct(Long userId, ProductPublishRequest ppr);

    Result<ProductDetailResponse> productDetail(Long productId);

    Result<Void> productUpdate(Long userId, UpdateProductDetailRequest updr, Long productId);

    Result<Void> removeProduct(Long userId, Long productId);

    Result<Void> deleteProduct(Long userId, Long productId);

    Result<Void> republishProduct(Long userId, Long productId);

    Result<IPage<Product>> showMyList(Long userId, PageRequest pageRequest);

    Result<IPage<Product>> showProductList(ProductListRequest productListRequestDTO);

    Result<IPage<Product>> recommendProducts(PageRequest pageRequest);

    Result<IPage<Product>> showByCategory(ShowProductByCategoryRequest request);

    Result<Void> upload(MultipartFile file, Long userId, Long productId);
}
