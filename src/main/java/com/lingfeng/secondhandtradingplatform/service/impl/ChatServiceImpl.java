package com.lingfeng.secondhandtradingplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingfeng.secondhandtradingplatform.DTO.Result;
import com.lingfeng.secondhandtradingplatform.DTO.request.ChatMessageRequest;
import com.lingfeng.secondhandtradingplatform.DTO.response.ConversationCreateResponse;
import com.lingfeng.secondhandtradingplatform.DTO.response.ConversationDetailResponse;
import com.lingfeng.secondhandtradingplatform.DTO.response.ConversationInListResponse;
import com.lingfeng.secondhandtradingplatform.DTO.response.MessageDetailResponse;
import com.lingfeng.secondhandtradingplatform.mapper.ConversationMapper;
import com.lingfeng.secondhandtradingplatform.mapper.MessageMapper;
import com.lingfeng.secondhandtradingplatform.mapper.ProductMapper;
import com.lingfeng.secondhandtradingplatform.mapper.UserMapper;
import com.lingfeng.secondhandtradingplatform.pojo.Conversation;
import com.lingfeng.secondhandtradingplatform.pojo.Message;
import com.lingfeng.secondhandtradingplatform.pojo.Product;
import com.lingfeng.secondhandtradingplatform.pojo.User;
import com.lingfeng.secondhandtradingplatform.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional(rollbackFor = Exception.class)
@Slf4j
@Service
public class ChatServiceImpl extends ServiceImpl<ConversationMapper,Conversation> implements ChatService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MessageMapper messageMapper;


    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    //创建会话
    @Override
    public Result<ConversationCreateResponse> conversationCreate(Long productId, Long buyerId) {
        log.info("创建会话请求:userId={},productId={}",buyerId,productId);

        Product product = productMapper.selectById(productId);
        if(product == null){
            log.info("创建会话失败: 商品不存在, productId={}", productId);
            return Result.error(404,"商品不存在");
        }
        Long sellerId = product.getUserId();

        String conversationId = productId + "_" + sellerId + "_" + buyerId;
        Conversation exiting = getById(conversationId);
        if(exiting != null){
            log.info("会话已存在:conversationId={}",conversationId);
            ConversationCreateResponse response = buildConversationCreateResponse(exiting,buyerId);
            return Result.success(response);
        }

        // 4. 创建新会话
        Conversation conversation = new Conversation();
        conversation.setId(conversationId);
        conversation.setProductId(productId);
        conversation.setSellerId(sellerId);
        conversation.setBuyerId(buyerId);
        conversation.setIsActive(true);

        save(conversation);
        log.info("创建会话成功: conversationId={}", conversationId);

        // 5. 组装返回数据
        ConversationCreateResponse response = buildConversationCreateResponse(conversation, buyerId);

        return Result.success(response);
    }

    //发送消息
    @Override
    public Result<Void> sendMessage(ChatMessageRequest request, Principal principal) {
        Long fromUserId = Long.valueOf(principal.getName());
        log.info("发送消息请求:from={},to={},content={}",fromUserId,request.getToUserId(),request.getContent());

        //检验会话是否存在
        Conversation conversation = getById(request.getConversationId());
        if(conversation == null){
            log.error("发送消息失败:会话不存在,conversationId={}",request.getConversationId());
            return Result.error(404,"会话不存在");
        }

        //校验用户是否属于会话
        boolean isBuyer = conversation.getBuyerId().equals(fromUserId);
        boolean isSeller = conversation.getSellerId().equals(fromUserId);
        if(!isSeller && !isBuyer){
            log.error("发送消息失败:用户无权发送消息,userId={}, conversationId={}", fromUserId, request.getConversationId());
            return Result.error(403,"无权限");
        }

        //校验会话是否有效
        if (!conversation.getIsActive()){
            log.error("会话已关闭: conversationId={}", request.getConversationId());
            return Result.error(404,"会话不存在");
        }

        // 4. 保存消息到数据库
        Message message = new Message();
        message.setConversationId(request.getConversationId());
        message.setFromUserId(fromUserId);
        message.setToUserId(request.getToUserId());
        message.setContent(request.getContent());
        message.setMsgType(request.getMsgType() != null ? request.getMsgType() : 1);
        message.setIsRead(false);

        messageMapper.insert(message);

        // 5. 更新会话的最后消息和未读数
        conversation.setLastMessage(request.getContent());
        conversation.setLastMsgTime(LocalDateTime.now());

        // 根据发送者身份，更新对方的未读数
        if (isBuyer) {
            // 买家发的，卖家未读数+1
            conversation.setSellerUnread(conversation.getSellerUnread() + 1);
        } else {
            // 卖家发的，买家未读数+1
            conversation.setBuyerUnread(conversation.getBuyerUnread() + 1);
        }
        updateById(conversation);

        // 6. 推送给接收方
        messagingTemplate.convertAndSendToUser(
                String.valueOf(request.getToUserId()),
                "/queue/messages",
                message
        );

        log.info("消息已推送: toUserId={}", request.getToUserId());

        return Result.success();
    }

    //查询会话列表
    @Override
    public Result<List<ConversationInListResponse>> getList(Long userId) {

        log.info("获取会话列表请求:userId={}",userId);

        List<Conversation> conversationList = query().eq("buyer_id",userId).or().eq("seller_id",userId).list();

        List<ConversationInListResponse> responses = buildConversationInListResponses(conversationList,userId);

        log.info("获取会话列表成功:userId={}",userId);
        return Result.success(responses);
    }

    //查询会话详情
    @Override
    public Result<ConversationDetailResponse> getConversationDetail(String conversationId, Long userId) {

        log.info("查询会话详情请求:conversationId={},userId={}",conversationId,userId);

        ConversationDetailResponse response = buildConversationDetailResponse(conversationId,userId);

        log.info("查询会话详情成功:conversationId={},userId={}",conversationId,userId);
        return Result.success(response);
    }

    //查询历史消息
    //用于用户发送新消息后，直接刷新消息列表，不需要加载会话详情
    @Override
    public Result<List<MessageDetailResponse>> getMessages(String conversationId, Long userId) {

        log.info("查询历史消息请求:conversationId={},userId={}",conversationId,userId);

        List<MessageDetailResponse> responses = buildMessageDetailResponses(conversationId,userId);

        log.info("查询历史消息成功:conversationId={},userId={}",conversationId,userId);
        return Result.success(responses);
    }

    //标记消息已读
    @Override
    public Result<Void> markIsRead(String conversationId, Long userId) {

        log.info("标记消息已读请求:conversationId={},userId={}",conversationId,userId);

        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getConversationId,conversationId)
                .eq(Message::getToUserId,userId)
                .eq(Message::getIsRead,false);

        Message updateMessage = new Message();
        updateMessage.setIsRead(true);

        messageMapper.update(updateMessage,wrapper);

        Conversation conversation = getById(conversationId);
        if(conversation == null){
            log.error("标记消息已读失败:会话不存在,conversationId={},userId={}",conversation,userId);
            return Result.error(404,"会话不存在");
        }

        //会话表未读清零
        if (conversation != null) {
            if (userId.equals(conversation.getBuyerId())) {
                conversation.setBuyerUnread(0);
            } else if (userId.equals(conversation.getSellerId())) {
                conversation.setSellerUnread(0);
            }
            updateById(conversation);
        }

        log.info("标记消息已读成功:conversationId={},userId={}",conversationId,userId);
        return Result.success();
    }

    /**
     * 组装返回数据
     */
    private ConversationCreateResponse buildConversationCreateResponse(Conversation conversation, Long currentUserId) {
        ConversationCreateResponse response = new ConversationCreateResponse();

        // 基础数据
        response.setConversationId(conversation.getId());
        response.setProductId(conversation.getProductId());
        response.setLastMessage(conversation.getLastMessage());
        response.setLastMsgTime(conversation.getLastMsgTime());
        response.setIsActive(conversation.getIsActive());

        // 商品信息
        Product product = productMapper.selectById(conversation.getProductId());
        if (product != null) {
            response.setProductTitle(product.getTitle());
            response.setProductPrice(product.getPrice());
            // product.getImages() 可能是逗号分隔的多张图片，取第一张
            String images = product.getImages();
            response.setProductImage(images != null && images.contains(",")
                    ? images.split(",")[0] : images);
        }

        // 对方信息（当前用户是买家，对方就是卖家；当前用户是卖家，对方就是买家）
        Long otherUserId = currentUserId.equals(conversation.getBuyerId())
                ? conversation.getSellerId()
                : conversation.getBuyerId();
        response.setOtherUserId(otherUserId);

        User otherUser = userMapper.selectById(otherUserId);
        if (otherUser != null) {
            response.setOtherUsername(otherUser.getUsername());
            response.setOtherUserImg(otherUser.getImg());
        }

        // 未读数
        Integer unreadCount = currentUserId.equals(conversation.getBuyerId())
                ? conversation.getBuyerUnread()
                : conversation.getSellerUnread();
        response.setUnreadCount(unreadCount);

        return response;
    }

    /**
     * 组装返回数据
     */
    private List<ConversationInListResponse> buildConversationInListResponses(List<Conversation> conversationList,Long currentUserId){

        if(conversationList.isEmpty()){
            return Collections.emptyList();
        }

        List<ConversationInListResponse> responses = new ArrayList<>();


        List<Long> userIds = new ArrayList<>();
        for(Conversation conversation:conversationList){
            Long otherUserId = currentUserId.equals(conversation.getBuyerId())
                    ? conversation.getSellerId()
                    : conversation.getBuyerId();

            userIds.add(otherUserId);
        }
        List<User> userList = userMapper.selectBatchIds(userIds);
        //转成map直接取出，更快
        Map<Long,User> userMap = userList
                                .stream()
                                //u -> u代表原样返回
                                .collect(Collectors.toMap(User::getId,u -> u));

        for(Conversation conversation:conversationList){
            ConversationInListResponse response = new ConversationInListResponse();

            String conversationId = conversation.getId();

            Long otherUserId = currentUserId.equals(conversation.getBuyerId())
                    ? conversation.getSellerId()
                    : conversation.getBuyerId();

            User otherUser = userMap.get(otherUserId);
            if (otherUser != null) {
                response.setOtherUserId(otherUserId);
                response.setOtherUserImg(otherUser.getImg());
                response.setOtherUsername(otherUser.getUsername());
            } else {
                response.setOtherUserId(otherUserId);
                response.setOtherUserImg(null);
                response.setOtherUsername("用户已注销");
            }


            String lastMessage = conversation.getLastMessage();
            LocalDateTime lastMsgTime = conversation.getLastMsgTime();

            Integer unreadCount = currentUserId.equals(conversation.getBuyerId())
                    ? conversation.getBuyerUnread()
                    : conversation.getSellerUnread();

            response.setConversationId(conversationId);
            response.setLastMessage(lastMessage);
            response.setLastMsgTime(lastMsgTime);
            response.setUnreadCount(unreadCount);

            responses.add(response);
        }

        return responses;
    }

    /**
     * 组装返回数据
     */
    private  List<MessageDetailResponse> buildMessageDetailResponses(String conversationId,Long currentUserId){

        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getConversationId,conversationId)
                .orderByAsc(Message::getSendTime);

        List<Message> messageList = messageMapper.selectList(wrapper);

        if (messageList.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 收集所有发送方用户ID（去重）
        List<Long> userIds = messageList.stream()
                .map(Message::getFromUserId)
                .distinct()  // 去重，只有 2 个
                .collect(Collectors.toList());

        // 3. 批量查询用户（只查 1 次，不是 100 次）
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<MessageDetailResponse> responses = new ArrayList<>();

        for(Message message:messageList){
            MessageDetailResponse response = new MessageDetailResponse();

            Long messageId = message.getId();

            Long fromUserId = message.getFromUserId();

             User fromUser = userMap.get(fromUserId);
            if (fromUser != null) {
                response.setFromUserId(fromUserId);
                response.setFromUserImg(fromUser.getImg());
                response.setFromUsername(fromUser.getUsername());
            } else {
                response.setFromUserId(fromUserId);
                response.setFromUserImg(null);
                response.setFromUsername("用户已注销");
            }

            String content = message.getContent();
            Integer msgType = message.getMsgType();

            LocalDateTime sendTime = message.getSendTime();

            Boolean isRead = message.getIsRead();

            Boolean isSelf = currentUserId.equals(message.getFromUserId());

            response.setMessageId(messageId);
            response.setContent(content);
            response.setMsgType(msgType);
            response.setSendTime(sendTime);
            response.setIsRead(isRead);
            response.setIsSelf(isSelf);

            responses.add(response);
        }

        return responses;
    }

    /**
     * 组装返回数据
     */
    private ConversationDetailResponse buildConversationDetailResponse(String conversationId,Long currentUserId){
        ConversationDetailResponse response = new ConversationDetailResponse();

        Conversation conversation = getById(conversationId);

        Long otherUserId = currentUserId.equals(conversation.getBuyerId())
                ? conversation.getSellerId()
                : conversation.getBuyerId();

        User otherUser = userMapper.selectById(otherUserId);
        if (otherUser != null) {
            response.setOtherUserId(otherUserId);
            response.setOtherUserImg(otherUser.getImg());
            response.setOtherUsername(otherUser.getUsername());
        } else {
            response.setOtherUserId(otherUserId);
            response.setOtherUserImg(null);
            response.setOtherUsername("用户已注销");
        }

        Long productId = conversation.getProductId();
        Product product = productMapper.selectById(productId);
        if(product != null){
            response.setProductId(productId);
            response.setProductTitle(product.getTitle());
            response.setProductPrice(product.getPrice());
            response.setProductImg(product.getImages());
        }else{
            response.setProductId(productId);
            response.setProductTitle("商品不存在");
            response.setProductPrice(null);
            response.setProductImg(null);
        }

        List<MessageDetailResponse> responses = buildMessageDetailResponses(conversationId,currentUserId);

        response.setConversationId(conversationId);
        response.setResponses(responses);

        return response;
    }
}
