package com.lingfeng.secondhandtradingplatform.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
    @NotBlank(message = "商品信息不能为空")
    @Size(min = 1, max = 100, message = "标题长度1-100字")
    private String title;

    //商品描述
    @Size(max = 100, message = "描述不能超过100字")
    private String description;

    //分类
    @NotBlank(message = "商品信息不能为空")
    private String category;

    //售价
    @NotNull(message = "商品信息不能为空")
    @DecimalMin(value = "0.01", message = "售价必须大于0,小于999999.99")
    @DecimalMax(value = "999999.99", message = "售价必须大于0,小于999999.99")
    private BigDecimal price;

    //原价
    @DecimalMin(value = "0.01", message = "售价必须大于0,小于999999.99")
    @DecimalMax(value = "999999.99", message = "售价必须大于0,小于999999.99")
    private BigDecimal originalPrice;

    //图片
    @Size(max = 1000, message = "图片地址过长")
    private String images;

    //商品成色
    @NotNull(message = "商品信息不能为空")
    @Min(value = 1, message = "成色参数错误")
    @Max(value = 3, message = "成色参数错误")
    private Integer productCondition;

    //商品状态
    private Integer status;

    //所在城市
    @NotBlank(message = "商品信息不能为空")
    private String city;

    //交易方式
    @Min(value = 1, message = "交易方式参数错误")
    @Max(value = 3, message = "交易方式参数错误")
    @NotNull(message = "商品信息不能为空")
    private Integer tradeType;

    //浏览次数
    private Integer viewCount;

    //点赞次数
    private Integer likeCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer stock;

    private Integer collectCount;
}