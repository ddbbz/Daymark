package com.ddbz.daymark.pojo.vo.function;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentVO {
    private int id;
    private int postId;
    private int userId;
    private String content;
    private LocalDateTime createdTime;
    private boolean commentObjectIsPost;
}
