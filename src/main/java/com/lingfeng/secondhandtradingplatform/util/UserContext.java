package com.lingfeng.secondhandtradingplatform.util;

public class UserContext {

    private static final ThreadLocal<Long> userHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> tokenHolder = new ThreadLocal<>();

    public static void setUserId(Long userId){
        userHolder.set(userId);
    }

    public static Long getUserId(){
        return userHolder.get();
    }

    public static void setToken(String token){
        tokenHolder.set(token);
    }

    public static String getToken(){
        return tokenHolder.get();
    }


    public static void clear(){
        userHolder.remove();
        tokenHolder.remove();
    }
}
