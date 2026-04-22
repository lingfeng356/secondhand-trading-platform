package com.lingfeng.secondhandtradingplatform.DTO;

import lombok.Data;

@Data

public class Result{

    //状态码
    private Integer code;
    //提示信息
    private String  message;
    //返回数据
    private Object data;

    public Result() {
    }
    //1.不返回数据的成功
    public static Result success(){
        Result result = new Result();
        result.setCode(200);
        result.setMessage("OK");
        return result;
    }
    //2.返回数据的成功
    public static Result success(Object data){
        Result result = new Result();
        result.setCode(200);
        result.setMessage("OK");
        result.setData(data);
        return result;
    }
    //3.失败
    public static Result error(String message){
        Result result = new Result();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }
    //4.自定义状态码的失败
    public static Result error(Integer code,String message){
        Result result = new Result();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

}
