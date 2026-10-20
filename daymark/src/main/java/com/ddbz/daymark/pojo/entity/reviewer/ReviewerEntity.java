package com.ddbz.daymark.pojo.entity.reviewer;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("reviewer")
public class ReviewerEntity {
    private Integer id;
    private String uid;
    private String password;
}
