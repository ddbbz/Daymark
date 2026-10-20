package com.ddbz.daymark.pojo.vo;

import lombok.Data;

@Data
public class LoginVO {
    private String jwt;
    private int role;
}
