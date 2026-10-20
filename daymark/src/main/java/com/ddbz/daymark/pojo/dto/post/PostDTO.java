package com.ddbz.daymark.pojo.dto.post;

import lombok.Data;

import java.util.List;

@Data
public class PostDTO {
    private int id;
    private int userId;
    private String title;                   //帖子标题
    private String content;                 //帖子正文
    private List<String> image;      //图片
    private String likeCount;               //点赞数
    private String commentCount;            //评论数
    private String favoriteCount;            //收藏数
}
