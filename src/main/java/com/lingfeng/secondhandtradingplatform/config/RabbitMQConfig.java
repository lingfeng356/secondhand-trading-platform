package com.lingfeng.secondhandtradingplatform.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RabbitMQConfig {

    //商品相关
    public static final String PRODUCT_EXCHANGE = "product.exchange";
    public static final String PRODUCT_VIEWS_UPDATE_QUEUE = "product.view.update.queue";
    public static final String PRODUCT_VIEWS_ROUTING_KEY = "product.view.key";
    public static final String PRODUCT_LIKES_UPDATE_QUEUE = "product.like.update.queue";
    public static final String PRODUCT_LIKES_ROUTING_KEY = "product.like.key";

    //订单相关
    public static final String ORDER_CREATE_QUEUE = "order.create.queue";
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_CREATE_KEY = "order.create.key";


    //订单商品相关
    public static final String ORDERITEM_CREATE_QUEUE = "orderItem.create.queue";
    public static final String ORDERITEM_EXCHANGE = "orderItem.exchange";
    public static final String ORDERITEM_CREATE_KEY = "orderItem.create.key";

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        // 只在这里设置一次
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.debug("消息发送成功: {}", correlationData);
            } else {
                log.error("消息发送失败: {}", cause);
            }
        });

        //将消息转换器配置到rabbitMQ中
        rabbitTemplate.setMessageConverter(jsonMessageConverter());

        return rabbitTemplate;
    }

    //声明交换机
    @Bean
    public Exchange productExchange(){
        return ExchangeBuilder.topicExchange(PRODUCT_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Exchange orderExchange(){
        return ExchangeBuilder.topicExchange(ORDER_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Exchange orderItemExchange(){
        return ExchangeBuilder.topicExchange(ORDERITEM_EXCHANGE)
                .durable(true)
                .build();
    }

    //声明队列
    @Bean
    public Queue productViewsQueue(){
        return QueueBuilder.durable(PRODUCT_VIEWS_UPDATE_QUEUE).build();
    }

    @Bean
    public Queue productLikesQueue(){
        return QueueBuilder.durable(PRODUCT_LIKES_UPDATE_QUEUE).build();
    }


    @Bean
    public Queue orderCreateQueue(){
        return QueueBuilder.durable(ORDER_CREATE_QUEUE).build();
    }

    @Bean
    public Queue orderItemCreateQueue(){
        return QueueBuilder.durable(ORDERITEM_CREATE_QUEUE).build();
    }


    //绑定队列到交换机
    @Bean
    public Binding productViewsBinding(){
        return BindingBuilder
                .bind(productViewsQueue())
                .to(productExchange())
                .with(PRODUCT_VIEWS_ROUTING_KEY)
                .noargs();
    }

    @Bean
    public Binding productLikesBinding(){
        return BindingBuilder
                .bind(productLikesQueue())
                .to(productExchange())
                .with(PRODUCT_LIKES_ROUTING_KEY)
                .noargs();
    }

    @Bean
    public Binding orderCreateBinding(){
        return BindingBuilder
                .bind(orderCreateQueue())
                .to(orderExchange())
                .with(ORDER_CREATE_KEY)
                .noargs();
    }

    @Bean
    public Binding orderItemCreateBinding(){
        return BindingBuilder
                .bind(orderItemCreateQueue())
                .to(orderItemExchange())
                .with(ORDERITEM_CREATE_KEY)
                .noargs();
    }

    //配置消息转换器
    @Bean
    public MessageConverter jsonMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }

}
