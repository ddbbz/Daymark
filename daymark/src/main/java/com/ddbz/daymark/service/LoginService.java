package com.ddbz.daymark.service;

import com.ddbz.daymark.pojo.dto.usesr.LoginDTO;
import com.ddbz.daymark.pojo.vo.LoginVO;

public interface LoginService {
    public LoginVO judgeByUserNameAndPassword(LoginDTO loginDTO);
}
