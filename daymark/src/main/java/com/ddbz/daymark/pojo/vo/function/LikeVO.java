package com.ddbz.daymark.pojo.vo.function;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LikeVO {
    private int id;
    private int userId;
    private int postId;
    private LocalDateTime createdTime;
}
