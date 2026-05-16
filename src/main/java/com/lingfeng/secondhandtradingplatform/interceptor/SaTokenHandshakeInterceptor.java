package com.lingfeng.secondhandtradingplatform.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.security.Principal;
import java.util.Map;

public class SaTokenHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String uri = request.getURI().toString();
        String token = null;
        if (uri.contains("?token=")) {
            token = uri.split("\\?token=")[1].split("&")[0];
        }

        System.out.println("WebSocket握手，token: " + token);

        if (token != null) {
            try {
                Object loginId = StpUtil.getLoginIdByToken(token);
                if (loginId != null) {
                    String userId = loginId.toString();

                    attributes.put("loginId", userId);
                    attributes.put("user", new Principal() {
                        @Override
                        public String getName() {
                            return userId;
                        }
                    });

                    System.out.println("WebSocket握手成功，用户ID: " + userId);
                    return true;
                }
            } catch (Exception e) {
                System.out.println("Token验证失败: " + e.getMessage());
            }
        }
        System.out.println("WebSocket握手失败，拒绝连接");
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}