package com.lingfeng.secondhandtradingplatform.constant;

public class RedisConstant {
    public static final String LOGIN_CODE_KEY = "login:code:phone:";
    public static final long LOGIN_CODE_TTL = 60L;

    public static final String LOGIN_TOKEN_KEY = "login:token:";
    public static final long LOGIN_USER_TTL = 30L;

    public static final String PRODUCT_KEY = "product:";
    public static final long PRODUCT_TTL = 30L;

    public static final String ORDER_KEY = "order:";
    public static final long ORDER_TTL = 30L;
}
