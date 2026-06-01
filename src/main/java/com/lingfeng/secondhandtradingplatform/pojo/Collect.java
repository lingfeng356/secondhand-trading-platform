package com.lingfeng.secondhandtradingplatform.pojo;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Collect {

    private Long id;
    private Long userId;
    private Long productId;
    private LocalDateTime createTime;
}
