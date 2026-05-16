package com.lingfeng.secondhandtradingplatform.DTO.response;

import com.lingfeng.secondhandtradingplatform.pojo.Message;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class ConversationDetailResponse {

    private String conversationId;

    private Long otherUserId;
    private String otherUserImg;
    private String otherUsername;

    private Long productId;
    private String productTitle;
    private BigDecimal productPrice;
    private String productImg;

    private List<MessageDetailResponse> responses;

}
