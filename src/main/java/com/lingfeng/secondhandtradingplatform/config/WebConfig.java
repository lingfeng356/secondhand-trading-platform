package com.lingfeng.secondhandtradingplatform.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // ========== 用户模块（不需要登录）==========
                        "/user/loginByCode",        // 验证码登录
                        "/user/loginByPassword",    // 密码登录
                        "/user/sendCode",           // 发送验证码
                        "/user/register",           // 用户注册
                        "/user/resetPwd",           // 重置密码

                        // ========== 商品模块（不需要登录）==========
                        "/product/list",                    // 商品列表搜索
                        "/product/detail/**",               // 商品详情
                        "/product/recommendProducts",       // 首页推荐

                        // ========== 静态资源 ==========
                        "/css/**",
                        "/js/**",
                        "/img/**",
                        "/html/**",
                        "/favicon.ico",

                        // ========== 接口文档 ==========
                        "/doc.html",
                        "/swagger-resources/**",
                        "/v3/api-docs/**",
                        "/webjars/**",

                        // ========== 错误页面 ==========
                        "/error"
                );
    }
}