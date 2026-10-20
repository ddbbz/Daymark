package com.ddbz.daymark.service;

import com.ddbz.daymark.pojo.dto.usesr.RegisterDTO;
import com.ddbz.daymark.pojo.vo.user.RegisterDefaultVO;

import java.io.IOException;

public interface RegisterService {
    public Boolean register(RegisterDTO registerDTO) throws IOException;

    public RegisterDefaultVO createDefaultInformal();
}
