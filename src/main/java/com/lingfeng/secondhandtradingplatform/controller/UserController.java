package com.lingfeng.secondhandtradingplatform.controller;


import com.lingfeng.secondhandtradingplatform.DTO.Result;
import com.lingfeng.secondhandtradingplatform.pojo.User;
import com.lingfeng.secondhandtradingplatform.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    //查询用户信息
    @GetMapping("/user/getUser")
    public Result getByToken(@RequestHeader("token") String token){
        return userService.getByToken(token);
    }

    //编辑用户信息
    @PostMapping("/user/updateUser")
    public Result updateByToken(@RequestHeader("token") String token,@RequestBody User user){
        return userService.updateByToken(token,user);
    }
}
