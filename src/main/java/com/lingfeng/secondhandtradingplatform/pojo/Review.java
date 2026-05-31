package com.lingfeng.secondhandtradingplatform.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Review {

    private Long id;
    private Long productId;
    private Long userId;
    private String orderId;
    private Integer rating;
    private String content;
    private String replyContent;
    private LocalDateTime replyTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
