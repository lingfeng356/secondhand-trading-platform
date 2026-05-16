package com.lingfeng.secondhandtradingplatform.DTO.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ConversationInListResponse {

    private String conversationId;
    private Long otherUserId;
    private String otherUserImg;
    private String otherUsername;
    private String lastMessage;
    private LocalDateTime lastMsgTime;
    private Integer unreadCount;
}
