package com.ddbz.daymark.pojo.dto.function;

import lombok.Data;

@Data
public class CommentDTO {
    private int id;
    private int postId;
    private int userId;
    private String content;
    private boolean commentObjectIsPost;
}
