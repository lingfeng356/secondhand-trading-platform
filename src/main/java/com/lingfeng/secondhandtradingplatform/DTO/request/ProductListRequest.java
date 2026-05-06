package com.lingfeng.secondhandtradingplatform.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "商品列表查询请求")
public class ProductListRequest {

    @Schema(description = "页码", defaultValue = "1", example = "1")
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数", defaultValue = "10", example = "10")
    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数最大为100")
    private Integer pageSize = 10;

    @Schema(description = "商品分类", example = "手机")
    private String category;

    @Schema(description = "最低价格", example = "100")
    @Min(value = 0, message = "最低价格不能小于0")
    private Double minPrice;

    @Schema(description = "最高价格", example = "10000")
    @Min(value = 0, message = "最高价格不能小于0")
    private Double maxPrice;

    @Schema(description = "排序字段", example = "price", allowableValues = {"price", "createTime"})
    private String sortBy;

    @Schema(description = "排序方式", example = "desc", allowableValues = {"asc", "desc"})
    private String sortOrder;

    @Schema(description = "所在城市", example = "深圳")
    private String city;

    @Schema(description = "商品标题（模糊搜索）", example = "iPhone")
    private String title;
}