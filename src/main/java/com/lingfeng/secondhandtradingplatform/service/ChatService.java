package com.lingfeng.secondhandtradingplatform.service;

import com.lingfeng.secondhandtradingplatform.DTO.Result;
import com.lingfeng.secondhandtradingplatform.DTO.request.ChatMessageRequest;
import com.lingfeng.secondhandtradingplatform.DTO.response.ConversationCreateResponse;
import com.lingfeng.secondhandtradingplatform.DTO.response.ConversationDetailResponse;
import com.lingfeng.secondhandtradingplatform.DTO.response.ConversationInListResponse;
import com.lingfeng.secondhandtradingplatform.DTO.response.MessageDetailResponse;
import com.lingfeng.secondhandtradingplatform.pojo.Conversation;
import com.lingfeng.secondhandtradingplatform.pojo.Message;

import java.util.List;

import java.security.Principal;

public interface ChatService {
    Result<ConversationCreateResponse> conversationCreate(Long productId, Long buyerId);

    Result<Void> sendMessage(ChatMessageRequest request, Principal principal);

    Result<List<ConversationInListResponse>> getList(Long userId);

    Result<ConversationDetailResponse> getConversationDetail(String conversationId, Long userId);

    Result<List<MessageDetailResponse>> getMessages(String conversationId, Long userId);

    Result<Void> markIsRead(String conversationId, Long userId);
}
