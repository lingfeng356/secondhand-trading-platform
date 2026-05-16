package com.lingfeng.secondhandtradingplatform.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Conversation {

    private String id;
    private Long productId;
    private Long sellerId;
    private Long buyerId;
    private String lastMessage;
    private LocalDateTime lastMsgTime;
    private Integer sellerUnread;
    private Integer buyerUnread;
    private Boolean isActive;
}
