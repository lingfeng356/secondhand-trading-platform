package com.lingfeng.secondhandtradingplatform.interceptor;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

import java.security.Principal;
import java.util.Map;

public class UserChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();

            if (sessionAttributes != null) {
                // 从握手时放入的attributes中取出loginId或user
                Object loginId = sessionAttributes.get("loginId");
                Object user = sessionAttributes.get("user");

                if (user instanceof Principal) {
                    // 如果HandshakeInterceptor已经创建了Principal对象
                    accessor.setUser((Principal) user);
                } else if (loginId != null) {
                    // 否则用loginId创建Principal
                    final String userId = loginId.toString();
                    accessor.setUser(new Principal() {
                        @Override
                        public String getName() { return userId; }
                    });
                }
            }
        }
        return message;
    }
}