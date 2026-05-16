package com.lingfeng.secondhandtradingplatform.config;

import com.lingfeng.secondhandtradingplatform.interceptor.SaTokenHandshakeInterceptor;
import com.lingfeng.secondhandtradingplatform.interceptor.UserChannelInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker//告诉spring开启websocket中的stomp协议
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Lazy
    @Autowired
    private TaskScheduler taskScheduler;

    //创建连接端点,即为客户端连接的大门,聊天需要通过一个可以连接的url，类似接口
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        //告诉客户端，服务器的聊天入口在/chat
        //withSockJS() 的作用是开启 SockJS 回退机制，解决 WebSocket 兼容性问题。
        registry.addEndpoint("/chat")
                .addInterceptors(new SaTokenHandshakeInterceptor())
                .setAllowedOriginPatterns("*")
                .withSockJS(); // 建议开启 SockJS 回退
    }

    //设置消息路由规则，判断不同的前缀url的消息应该发往哪些地方,或者自己当作邮局处理消息
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 3. 规则A：如果前端发消息到 "/app/...", 那就去找后端带 @MessageMapping 的方法
        registry.setApplicationDestinationPrefixes("/app");

        // 4. 规则B：开启内置邮局，并设定它负责处理 /topic (广播) 和 /queue (私聊) 格式的地址
        registry.enableSimpleBroker("/topic","/queue")
                //配置心跳任务
                .setHeartbeatValue(new long[] {10000, 20000})
                //配置定时器执行心跳任务
                .setTaskScheduler(taskScheduler);
    }

    //配置websocket的底层属性
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        // 设置消息大小限制为 5MB (5 * 1024 * 1024)
        registry.setMessageSizeLimit(5 * 1024 * 1024);

        // 设置发送缓冲区大小
        registry.setSendBufferSizeLimit(10 * 1024 * 1024);

        // 设置发送超时时间 (毫秒)
        registry.setSendTimeLimit(20000);

        // 设置等待第一条消息的超时时间 (毫秒)
        registry.setTimeToFirstMessage(30000);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new UserChannelInterceptor());
    }
}
