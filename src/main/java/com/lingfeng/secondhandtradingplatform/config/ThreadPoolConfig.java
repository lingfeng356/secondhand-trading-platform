package com.lingfeng.secondhandtradingplatform.config;

import com.lingfeng.secondhandtradingplatform.factory.CustomThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableScheduling
@Slf4j
public class ThreadPoolConfig implements SchedulingConfigurer {

    // 重试拦截器配置
    @Bean
    public RetryOperationsInterceptor retryInterceptor() {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(3)                    // 最多重试3次（包括第1次）
                .backOffOptions(1000, 2.0, 10000)  // 间隔1秒，倍数2，最大10秒
                .recoverer(new RejectAndDontRequeueRecoverer())  // 重试失败后拒绝并丢弃
                .build();
    }

    //定时任务线程池
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {

        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        //线程池大小
        scheduler.setPoolSize(10);

        //配置线程工厂
        scheduler.setThreadFactory(new CustomizableThreadFactory("scheduled-"));

        //等待60秒后关闭
        scheduler.setAwaitTerminationSeconds(60);

        //等待任务完成
        scheduler.setWaitForTasksToCompleteOnShutdown(true);

        //自定义异常处理器
        scheduler.setErrorHandler(t -> {
            String threadName = Thread.currentThread().getName();
            log.error("线程:{}定时任务执行异常:{}",threadName,t.getMessage());
        });

        //拒绝策略
        scheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        //初始化调度器
        scheduler.initialize();

        //注册到任务注册器,将调度器交给spring管理
        taskRegistrar.setScheduler(scheduler);
    }

    //RabbitMQ消费线程池
    @Bean("mqConsumerExecutor")
    public ThreadPoolTaskExecutor mqConsumerExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        //核心线程数
        executor.setCorePoolSize(10);

        //允许回收空闲核心线程
        executor.setAllowCoreThreadTimeOut(true);

        //最大线程数
        executor.setMaxPoolSize(50);

        //队列容量
        executor.setQueueCapacity(200);

        //空闲存活时间
        executor.setKeepAliveSeconds(60);

        //配置线程工厂
        executor.setThreadFactory(new CustomThreadFactory("mq-consumer-"));

        //拒绝策略,谁提交的任务谁自己执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        //关闭线程池时，等待正在执行的任务完成
        executor.setWaitForTasksToCompleteOnShutdown(true);

        //最多等多少秒，超时后强制关闭
        executor.setAwaitTerminationSeconds(60);

        //初始化线程工厂
        executor.initialize();
        return executor;
    }

    //RabbitMQ 监听器容器工厂
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            SimpleRabbitListenerContainerFactoryConfigurer configurer){

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();

        //把 Spring Boot 配置文件中的 RabbitMQ 配置自动应用到你的 factory 上。
        configurer.configure(factory,connectionFactory);

        //使用自定义线程池
        factory.setTaskExecutor(mqConsumerExecutor());

        //并发消费者数量
        factory.setConcurrentConsumers(10);
        factory.setMaxConcurrentConsumers(20);

        //每次拉取一条消息
        factory.setPrefetchCount(1);

        //开启手动确认机制
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);

        //重试机制,避免无限重试循环
        factory.setDefaultRequeueRejected(false);//拒绝后不重新入队
        factory.setAdviceChain(retryInterceptor());//重试拦截器

        //消费者标签策略,在rabbitMQ服务端标记消费者
        factory.setConsumerTagStrategy(queue -> "consumer-" + queue);

        //空闲间隔(没有消息时多久确认一次)
        factory.setIdleEventInterval(30000L);

        return factory;
    }
}
