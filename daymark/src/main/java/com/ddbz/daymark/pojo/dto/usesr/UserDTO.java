package com.ddbz.daymark.pojo.dto.usesr;

import lombok.Data;

@Data
public class UserDTO {
    private String bio;
    private String nickname;
    private String avatar;   // 头像 URL 字符串（由 /user/me/avatar 上传接口返回）
}
