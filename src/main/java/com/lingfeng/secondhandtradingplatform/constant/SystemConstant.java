package com.lingfeng.secondhandtradingplatform.constant;

public class SystemConstant {
    public static final String LOGIN_CODE_KEY = "login:code:phone:";
    public static final long LOGIN_CODE_TTL = 60L;
    public static final String LOGIN_LIMIT_KEY = "login:limit:phone:";
    public static final long LOGIN_LIMIT_TTL = 1L;

    public static final String LOGIN_USER_KEY = "login:user:";
    public static final long LOGIN_USER_TTL = 30L;

    public static final String PRODUCT_KEY = "product:";
    public static final long PRODUCT_TTL = 5L;
    public static final String PRODUCT_LIKE_KEY = "product:like:";
    public static final String PRODUCT_COLLECT_KEY = "product:collect:";

    public static final String ORDER_KEY = "order:";
    public static final long ORDER_TTL = 30L;

    public static final String ORDER_LOCK_KEY = "order:lock:product:";
    public static final long ORDER_LOCK_AUTO_TTL = 30L;
    public static final long ORDER_LOCK_TTL = 3L;

    public static final Integer PRODUCT_STATUS_ONSALE = 0;
    public static final Integer PRODUCT_STATUS_SOLDOUT = 1;
    public static final Integer PRODUCT_STATUS_OFFSALE = 2;

    public static final String PRODUCT_HOT_LIST = "product:hot:list";
    public static final long PRODUCT_HOT_LIST_TTL = 5L;

    public static final Integer ORDER_STATUS_PENDING = 0;
    public static final Integer ORDER_STATUS_PROCESSING = 1;
    public static final Integer ORDER_STATUS_SHIPPED = 2;
    public static final Integer ORDER_STATUS_COMPLETED = 3;
    public static final Integer ORDER_STATUS_CANCELLED = 4;
    public static final Integer ORDER_STATUS_REFUNDED = 5;

    public static final Integer PAY_BY_WECHAT = 0;
}
