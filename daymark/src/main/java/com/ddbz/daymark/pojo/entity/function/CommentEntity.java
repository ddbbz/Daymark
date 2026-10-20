package com.ddbz.daymark.pojo.entity.function;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "comment")
public class CommentEntity {
    private Integer id;
    private int postId;
    private int userId;
    private String content;
    private LocalDateTime createdTime;
    private boolean commentObjectIsPost;
}
