package com.lingfeng.secondhandtradingplatform.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data//getter setter toString equals hashCode 全参构造
@NoArgsConstructor//无参构造方法
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private Integer sex;
    private String phone;
    private String password;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String img;
    private String address;
}
