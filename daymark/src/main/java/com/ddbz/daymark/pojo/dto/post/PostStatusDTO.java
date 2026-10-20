package com.ddbz.daymark.pojo.dto.post;

import lombok.Data;

@Data
public class PostStatusDTO {
    private int userId;
    private String uid;
    private int id;
    private int status;
    private String title;                   //帖子标题
    private String content;                 //帖子正文
}
