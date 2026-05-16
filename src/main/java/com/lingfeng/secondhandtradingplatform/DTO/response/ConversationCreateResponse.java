package com.lingfeng.secondhandtradingplatform.DTO.response;

import cn.hutool.extra.spring.SpringUtil;
import com.lingfeng.secondhandtradingplatform.mapper.ProductMapper;
import com.lingfeng.secondhandtradingplatform.mapper.UserMapper;
import com.lingfeng.secondhandtradingplatform.pojo.Conversation;
import com.lingfeng.secondhandtradingplatform.pojo.Product;
import com.lingfeng.secondhandtradingplatform.pojo.User;
import com.lingfeng.secondhandtradingplatform.service.ChatService;
import com.lingfeng.secondhandtradingplatform.service.ProductService;
import com.lingfeng.secondhandtradingplatform.service.UserService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ConversationCreateResponse {

    // 会话基本信息
    private String conversationId;      // 会话ID
    private Long productId;             // 商品ID
    private Boolean isActive;           // 会话是否有效

    // 商品信息（关联查询）
    private String productTitle;        // 商品标题
    private String productImage;        // 商品图片
    private BigDecimal productPrice;    // 商品价格

    // 对方信息（根据当前用户，返回对方的信息）
    private Long otherUserId;           // 对方用户ID
    private String otherUsername;   // 对方昵称
    private String otherUserImg;     // 对方头像

    // 聊天信息
    private String lastMessage;         // 最后一条消息
    private LocalDateTime lastMsgTime;  // 最后消息时间
    private Integer unreadCount;        // 当前用户的未读数
}
