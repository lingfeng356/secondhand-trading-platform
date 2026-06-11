package com.lingfeng.secondhandtradingplatform.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingfeng.secondhandtradingplatform.pojo.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
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

    //更新商品库存
    @Update("update product set stock = #{stock} , status = #{status} where id = #{productId}")
    int updateProductStockAndStatus(@Param("productId") Long productId
            ,@Param("stock") Integer stock,
                           @Param("status") Integer status);

    //查询我的收藏商品
    @Select("select p.* from product p inner join collect c on p.id = c.product_id " +
            "where c.user_id = #{userId} and p.deleted = 0 " +
            "order by c.create_time desc")
    Page<Product> batchCollectByUserId(Page<Product> page, @Param("userId") Long userId);

    //原子增加商品收藏量
    @Update("update product set collect_count = collect_count + 1 where id = #{productId}")
    int addProductCollects(@Param("productId") Long productId);

    //原子减少商品收藏量
    @Update("update product set collect_count = collect_count - 1 where id = #{productId}")
    int deleteProductCollects(@Param("productId") Long productId);
}
