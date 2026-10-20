package com.ddbz.daymark.pojo.vo.user;

import lombok.Data;

@Data
public class UserVO {
    private int id;          //用户的id
    private String uid;        //用户的uid
    private String nickname;        //用户名
    private String bio;           //用户的个人简介
    private String avatar ;          //用户的头像
}
