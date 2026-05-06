package com.lingfeng.secondhandtradingplatform.converter;

import com.lingfeng.secondhandtradingplatform.DTO.request.ProductPublishRequest;
import com.lingfeng.secondhandtradingplatform.DTO.request.UpdateProductDetailRequest;
import com.lingfeng.secondhandtradingplatform.DTO.response.ProductDetailResponse;
import com.lingfeng.secondhandtradingplatform.pojo.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductConverter {

    Product toEntity(ProductPublishRequest request);

    ProductPublishRequest toProductPublishRequest(Product product);

    Product toEntity(ProductDetailResponse request);

    ProductDetailResponse toProductDetailResponse(Product product);

    Product toEntity(UpdateProductDetailRequest request);

    ProductDetailResponse toUpdateProductDetailRequest(Product product);
}
