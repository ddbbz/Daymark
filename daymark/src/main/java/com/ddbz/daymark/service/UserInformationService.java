package com.ddbz.daymark.service;

import com.ddbz.daymark.pojo.dto.usesr.UserDTO;
import com.ddbz.daymark.pojo.dto.usesr.UserPasswordDTO;
import com.ddbz.daymark.pojo.vo.user.UserVO;

import java.io.IOException;

public interface UserInformationService {
    public UserVO getUserInformation(String uid);

    public void updateUserInformation(String uid, UserDTO userDTO) throws IOException;

    public void updateUserPassword(UserPasswordDTO userPasswordDTO);
}
