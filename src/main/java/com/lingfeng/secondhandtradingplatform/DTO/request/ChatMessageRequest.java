package com.lingfeng.secondhandtradingplatform.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChatMessageRequest {

    @NotBlank(message = "会话ID不能为空")
    private String conversationId;

    @NotNull(message = "接收方ID不能为空")
    private Long toUserId;

    @NotBlank(message = "消息内容不能为空")
    private String content;

    private Integer msgType;  // 1-文本，2-图片，3-语音，默认1
}
