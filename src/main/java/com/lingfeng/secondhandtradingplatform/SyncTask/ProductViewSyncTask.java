package com.lingfeng.secondhandtradingplatform.SyncTask;

import com.lingfeng.secondhandtradingplatform.mapper.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Slf4j
public class ProductViewSyncTask {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ProductMapper productMapper;


    //TODO:改掉keys命令
    //定时更新浏览量
    @Scheduled(fixedDelay = 10000)
    public void syncViewCounts(){
        log.debug("开始同步浏览量到数据库");

        Set<String> keys = stringRedisTemplate.keys("product:view:*");

        if(keys.isEmpty()){
            return;
        }

        for(String key:keys){
            Long productId = Long.valueOf(key.split(":")[2]);
            String incrementCountStr = stringRedisTemplate.opsForValue().get(key);

            if(incrementCountStr == null){
                continue;
            }

            Integer incrementCount = Integer.parseInt(incrementCountStr);

            //只更新有浏览量的商品
            if(incrementCount > 0){
                int updated = productMapper.batchIncrementViewCount(productId,incrementCount);

                if(updated > 0){
                    log.debug("商品{}浏览量+{},同步成功",productId,incrementCount);
                    //同步成功后删除redis
                    stringRedisTemplate.delete(key);
                }else{
                    log.error("商品{}不存在,浏览量同步失败",productId);
                    // 商品不存在，删除 Redis Key 避免重复失败
                    stringRedisTemplate.delete(key);
                }
            }

            log.debug("浏览量同步完成");
        }
    }

    //TODO:改掉keys命令
    //定时更新点赞量
    @Scheduled(fixedDelay = 10000)
    public void syncLikeCounts(){
        log.debug("开始同步点赞量到数据库");

        Set<String> keys = stringRedisTemplate.keys("product:like:*");

        if(keys.isEmpty()){
            return;
        }

        for(String key:keys) {
            Long productId = Long.valueOf(key.split(":")[2]);
            Long incrementCountL = stringRedisTemplate.opsForSet().size(key);

            Integer incrementCount = incrementCountL != null ? incrementCountL.intValue() : 0;

            if (incrementCount > 0) {
                int updated = productMapper.batchIncrementLikeCount(productId, incrementCount);

                if (updated > 0) {
                    log.debug("商品{}点赞量+{},同步成功", productId, incrementCount);
                } else {
                    log.error("商品{}不存在,点赞量同步失败", productId);
                }
            }

            Long newIncrementCount = stringRedisTemplate.opsForSet().size(key);
            if (newIncrementCount == 0){
                stringRedisTemplate.delete(key);
            }
        }
    }
}
