package com.lingfeng.secondhandtradingplatform.DTO;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductListRequestDTO {
    private Integer pageNum;
    private Integer pageSize;
    private String category;
    private Double minPrice;
    private Double maxPrice;
    private String sortBy;
    private String sortOrder;
    private String city;
    private String title;
}
