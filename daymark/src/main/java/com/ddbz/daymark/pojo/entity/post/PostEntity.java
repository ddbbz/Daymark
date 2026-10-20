package com.ddbz.daymark.pojo.entity.post;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "post", autoResultMap = true)
public class PostEntity {
    private Integer id;
    private int userId;
    private String title;
    private String content;
    @TableField(typeHandler = JacksonTypeHandler.class) //能够将传递进来的参数，转换为json类型
    private List<String> images;
    private int likeCount;
    private int commentCount;
    private int favoriteCount;
    private int status;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
