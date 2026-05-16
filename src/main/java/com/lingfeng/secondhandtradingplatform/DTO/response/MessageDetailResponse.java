package com.lingfeng.secondhandtradingplatform.DTO.response;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class MessageDetailResponse {

    private Long messageId;
    // === 发送方信息 ===
    private Long fromUserId;          // 发送方用户ID
    private String fromUsername;      // 发送方昵称
    private String fromUserImg;       // 发送方头像

    private String content;
    private Integer msgType;

    // === 时间 ===
    private LocalDateTime sendTime;   // 发送时间

    // === 状态 ===
    private Boolean isRead;           // 是否已读
    private Boolean isSelf;           // 是否当前用户发送的（true=自己发的，false=对方发的）
}
