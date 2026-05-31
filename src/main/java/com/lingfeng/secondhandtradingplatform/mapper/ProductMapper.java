package com.lingfeng.secondhandtradingplatform.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingfeng.secondhandtradingplatform.pojo.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    //批量更新商品浏览量
    @Update("update product set view_count = view_count + #{increment} where id = #{productId}")
    int batchIncrementViewCount(@Param("productId") Long productId,
                                @Param("increment") Integer increment);

    //批量更新商品点赞量
    @Update("update product set like_count = like_count + #{increment} where id = #{productId}")
    int batchIncrementLikeCount(@Param("productId") Long productId,
                                @Param("increment") Integer increment);

    //扣减商品库存
    @Update("update product set stock = stock - #{quantity} where id = #{productId} and stock = #{oldStock}")
    int updateProductStock(@Param("productId") Long productId,
                           @Param("quantity") int quantity,
                            @Param("oldStock") int oldStock);
}
