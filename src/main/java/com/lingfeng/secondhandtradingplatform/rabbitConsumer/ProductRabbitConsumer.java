package com.lingfeng.secondhandtradingplatform.rabbitConsumer;


import com.lingfeng.secondhandtradingplatform.config.RabbitMQConfig;
import com.lingfeng.secondhandtradingplatform.message.LikeMessage;
import com.lingfeng.secondhandtradingplatform.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProductRabbitConsumer {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ProductService productService;

    //聚合浏览量
    @RabbitListener(queues = RabbitMQConfig.PRODUCT_VIEWS_UPDATE_QUEUE)
    public void handleViewMessage(Long productId){
        if(productId == null){
            log.warn("收到无效的浏览消息:productId={}",productId);
            return;
        }

        //聚合浏览量，10秒后交给定时任务定时更新数据库
        //使用redis的increment原子操作，增加浏览量
        String key = "product:view:" + productId;
        stringRedisTemplate.opsForValue().increment(key);

        log.debug("商品{}浏览+1,当前累计{}",productId,stringRedisTemplate.opsForValue().get(key));
    }

    //聚合点赞量
    @RabbitListener(queues = RabbitMQConfig.PRODUCT_LIKES_UPDATE_QUEUE)
    public void handleLikeMessage(LikeMessage message){

        Long productId = message.getProductId();
        Long userId = message.getUserId();

        if(productId == null || userId == null){
            log.warn("收到无效的点赞消息:{}",message);
            return;
        }

        //聚合点赞量,10秒后交给定时任务定时更新点赞量
        String key = "product:like:" + productId;
        Long added = stringRedisTemplate.opsForSet().add(key,userId.toString());

        if(added != null && added > 0){
            log.info("点赞成功:userId={},productId={}",userId,productId);
        }else{
            log.warn("点赞失败:重复点赞,userId={},productId={}",userId,productId);
        }

        log.debug("商品{}当前累计{}",productId,stringRedisTemplate.opsForSet().size(key));
    }


}
