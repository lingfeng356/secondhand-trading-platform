package com.lingfeng.secondhandtradingplatform.DTO;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductListRequestDTO {
    private Integer pageNum;
    private Integer pageSize;
    private String category;
    private Double price;
    private String status;
    private String sortBy;
    private String sortOrder;
}
