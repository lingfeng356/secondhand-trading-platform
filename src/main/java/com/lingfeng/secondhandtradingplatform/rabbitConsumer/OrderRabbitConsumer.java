package com.lingfeng.secondhandtradingplatform.rabbitConsumer;

import com.lingfeng.secondhandtradingplatform.config.RabbitMQConfig;
import com.lingfeng.secondhandtradingplatform.mapper.OrderMapper;
import com.lingfeng.secondhandtradingplatform.message.OrderCreateMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class OrderRabbitConsumer {

    private OrderMapper orderMapper;

}
