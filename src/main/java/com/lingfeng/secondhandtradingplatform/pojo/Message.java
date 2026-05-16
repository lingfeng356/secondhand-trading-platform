package com.lingfeng.secondhandtradingplatform.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Message {

    private Long id;
    private String conversationId;
    private Long fromUserId;
    private Long toUserId;
    private String content;
    private Integer msgType;//1文本，2图片，3语音
    private LocalDateTime sendTime;
    private Boolean isRead;
}
