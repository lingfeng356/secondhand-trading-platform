package com.lingfeng.secondhandtradingplatform.aop;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Component
@Aspect
public class LogAspect {


    //记录controller和service方法耗时
    @Around("execution(* com.lingfeng.secondhandtradingplatform.controller.*.*(..)) ||" +
            " execution(* com.lingfeng.secondhandtradingplatform.service.*.*(..))")
    public Object recordTime(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        //获取类名和方法名
        String className = proceedingJoinPoint.getTarget().getClass().getSimpleName();
        String methodName = proceedingJoinPoint.getSignature().getName();

        //记录开始时间
        long begin = System.currentTimeMillis();

        //执行原方法
        Object result = proceedingJoinPoint.proceed();

        //记录结束时间
        long end = System.currentTimeMillis();

        //计算经过时间
        long duration = end - begin;

        log.info("{},{} 执行耗时:{}ms",className,methodName,duration);

        return result;
    }

    //查看前端返回参数
    // 记录 Controller 所有请求
    @Before("execution(* com.lingfeng.secondhandtradingplatform.controller.*.*(..))")
    public void logRequest(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        log.info("请求：{}，参数：{}", methodName, Arrays.toString(args));
    }
}
