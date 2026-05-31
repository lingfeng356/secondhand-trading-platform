package com.lingfeng.secondhandtradingplatform.rabbitProducer;


import com.lingfeng.secondhandtradingplatform.config.RabbitMQConfig;
import com.lingfeng.secondhandtradingplatform.message.LikeMessage;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class ProductRabbitProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    //发送浏览消息
    public void sendViewMessage(Long productId){
        if(productId == null){
            log.error("productId为空,不发送浏览消息");
            return;
        }

        //这是 RabbitMQ 的消息确认机制，用于确保消息可靠送达。
        CorrelationData correlationData = new CorrelationData(
                UUID.randomUUID().toString());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.PRODUCT_EXCHANGE,
                RabbitMQConfig.PRODUCT_VIEWS_ROUTING_KEY,
                productId,
                correlationData
        );

        log.debug("发送浏览消息: productId={}", productId);
    }

    //发送点赞消息
    public void sendLikeMessage(Long userId,Long productId){
        if(productId == null){
            log.warn("无效点赞消息:userId={},productId={}",userId,productId);
            return;
        }

        LikeMessage message = new LikeMessage();
        message.setUserId(userId);
        message.setProductId(productId);

        //这是 RabbitMQ 的消息确认机制，用于确保消息可靠送达。
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.PRODUCT_EXCHANGE,
                RabbitMQConfig.PRODUCT_LIKES_ROUTING_KEY,
                message,
                correlationData
        );

        log.debug("发送点赞消息: userId={},productId={}",userId,productId);
    }
}
