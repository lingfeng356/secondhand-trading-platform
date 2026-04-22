package com.lingfeng.secondhandtradingplatform.util;

public class UserUtils {

    public static boolean checkPhone(String phone){
        //判断手机号码是否为空或null
        if(phone == null || phone.isEmpty()){
            return false;
        }
        //判断手机号码是否符合格式
        if (!phone.matches("^1[3456789]\\d{9}$")) {
            return false;
        }

        return true;
    }
}
