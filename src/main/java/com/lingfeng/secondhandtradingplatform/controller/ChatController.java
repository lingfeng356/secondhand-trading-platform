package com.lingfeng.secondhandtradingplatform.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.lingfeng.secondhandtradingplatform.DTO.Result;
import com.lingfeng.secondhandtradingplatform.DTO.request.ChatMessageRequest;
import com.lingfeng.secondhandtradingplatform.DTO.response.ConversationCreateResponse;
import com.lingfeng.secondhandtradingplatform.DTO.response.ConversationDetailResponse;
import com.lingfeng.secondhandtradingplatform.DTO.response.ConversationInListResponse;
import com.lingfeng.secondhandtradingplatform.DTO.response.MessageDetailResponse;
import com.lingfeng.secondhandtradingplatform.service.ChatService;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.security.Principal;


@Slf4j
@RestController
@Schema(description = "聊天模块")
public class ChatController {

    @Autowired
    private ChatService chatService;

    //创建会话
    @GetMapping("/conversation/create")
    @Schema(description = "创建会话")
    @SaCheckLogin
    public Result<ConversationCreateResponse> conversationCreate(@RequestParam Long productId){
        Long buyerId = StpUtil.getLoginIdAsLong();
        return chatService.conversationCreate(productId,buyerId);
    }

    //发送消息
    @MessageMapping("/chat.send")
    @Schema(description = "发送消息")
    @SaCheckLogin
    public Result<Void> sendMessage(@Valid @Payload ChatMessageRequest request, Principal principal){
        return chatService.sendMessage(request,principal);
    }

    //获取用户会话列表
    @GetMapping("/conversation/getList")
    @Schema(description = "获取会话列表")
    @SaCheckLogin
    public Result<List<ConversationInListResponse>> getList(){
        Long userId = StpUtil.getLoginIdAsLong();
        return chatService.getList(userId);
    }

    //获取会话详情
    @GetMapping("/conversation/detail/{conversationId}")
    @Schema(description = "获取会话详情")
    @SaCheckLogin
    public Result<ConversationDetailResponse> getConversationDetail(@PathVariable String conversationId){
        Long userId = StpUtil.getLoginIdAsLong();
        return chatService.getConversationDetail(conversationId,userId);
    }

    //分页获取历史消息
    @GetMapping("/conversation/getMessages/{conversationId}")
    @Schema(description = "分页获取历史消息")
    @SaCheckLogin
    public Result<List<MessageDetailResponse>> getMessages(@PathVariable String conversationId){
        Long userId = StpUtil.getLoginIdAsLong();
        return chatService.getMessages(conversationId,userId);
    }

    //进入会话时标记消息已读
    @PostMapping("/conversation/markIsRead/{conversationId}")
    @Schema(description = "标记消息已读")
    @SaCheckLogin
    public Result<Void> markIsRead(@PathVariable String conversationId){
        Long userId = StpUtil.getLoginIdAsLong();
        return chatService.markIsRead(conversationId,userId);
    }
}
