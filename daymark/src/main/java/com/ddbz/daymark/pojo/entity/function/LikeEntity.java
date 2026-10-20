package com.ddbz.daymark.pojo.entity.function;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_like")
public class LikeEntity {
    private Integer id;
    private int userId;
    private int postId;
    private LocalDateTime createdTime;
}
