package com.lingfeng.secondhandtradingplatform.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Product {

    //商品id
    @TableId(type = IdType.AUTO)
    private Long id;

    //卖家id
    private Long userId;

    //商品名字
    private String title;

    //商品描述
    private String description;

    //分类
    private String category;

    //售价
    private BigDecimal price;

    //原价
    private BigDecimal originalPrice;

    //图片
    private String images;

    //商品成色
    private Integer productCondition;

    //商品状态
    private Integer status;

    //所在城市
    private String city;

    //交易方式
    private Integer tradeType;

    //浏览次数
    private Integer viewCount;

    //点赞次数
    private Integer likeCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer stock;

    private Integer collectCount;

    private Integer isDeleted;
}