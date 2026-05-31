package com.lingfeng.secondhandtradingplatform.rabbitProducer;

import com.lingfeng.secondhandtradingplatform.config.RabbitMQConfig;
import com.lingfeng.secondhandtradingplatform.message.OrderCreateMessage;
import com.lingfeng.secondhandtradingplatform.message.OrderItemCreateMessage;
import com.lingfeng.secondhandtradingplatform.pojo.OrderItem;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class OrderRabbitProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;
}
