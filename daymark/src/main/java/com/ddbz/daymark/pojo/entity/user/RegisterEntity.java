package com.ddbz.daymark.pojo.entity.user;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user")
public class RegisterEntity {
    private Integer id;
    private String uid;        //用户的uid
    private String password;        //用户的密码
    private String nickname;        //用户名
    private String bio;           //用户的个人简介
    private String avatar;          //用户的头像
}
