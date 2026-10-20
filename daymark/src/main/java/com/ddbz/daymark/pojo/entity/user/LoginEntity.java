package com.ddbz.daymark.pojo.entity.user;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user")
public class LoginEntity {
    private String uid;
    private Integer id;
    private String password;
}
