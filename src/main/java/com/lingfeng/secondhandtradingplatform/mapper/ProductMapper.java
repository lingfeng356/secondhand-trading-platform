package com.lingfeng.secondhandtradingplatform.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingfeng.secondhandtradingplatform.DTO.request.ProductPublishRequest;
import com.lingfeng.secondhandtradingplatform.DTO.request.UpdateProductDetailRequest;
import com.lingfeng.secondhandtradingplatform.DTO.response.ProductDetailResponse;
import com.lingfeng.secondhandtradingplatform.pojo.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {


}
