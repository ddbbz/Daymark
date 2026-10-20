package com.ddbz.daymark.controller.User;

import com.ddbz.daymark.context.BaseContext;
import com.ddbz.daymark.common.Result;
import com.ddbz.daymark.pojo.dto.usesr.UserDTO;
import com.ddbz.daymark.pojo.dto.usesr.UserPasswordDTO;
import com.ddbz.daymark.pojo.vo.user.UserVO;
import com.ddbz.daymark.service.UserInformationService;
import com.ddbz.daymark.util.ModifyUserAvatarPathFormatUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


/**
 * 用户数据的增删改查
 */
@Slf4j
@RestController
@RequestMapping("/user/me")
public class UserInformationController {

    @Autowired
    private UserInformationService userInformationService;

    @Autowired
    ModifyUserAvatarPathFormatUtil modifyUserAvatarPathFormatUtil;


    /**
     * 通过token获取用户信息
     * @return
     */
    @GetMapping
    public Result<UserVO> userInformation() {
        log.info("用户{}申请获得个人信息", BaseContext.getCurrentUid());
        UserVO userVO = userInformationService.getUserInformation(BaseContext.getCurrentUid());
        if(userVO == null){
            log.info("用户{}不存在",BaseContext.getCurrentUid());
            return Result.error("用户不存在");
        }
        log.info("用户{}成功获得个人信息",BaseContext.getCurrentUid());
        return Result.success(userVO);
    }

    /**
     * 修改用户个人资料
     * @param userDTO
     * @return
     */
    @PutMapping("/update/information")
    public Result<String> updateUserInformation(@RequestBody UserDTO userDTO) throws IOException {
        log.info("用户{}申请修改用户名称",BaseContext.getCurrentUid());
        userInformationService.updateUserInformation(BaseContext.getCurrentUid(),userDTO);
        return Result.success("用户"+BaseContext.getCurrentUid()+"信息修改成功");
    }


    /**
     * 修改用户密码
     * @param userPasswordDTO
     * @return
     */
    @PutMapping("update/password")
    public Result<String> updateUserPassword(@RequestBody UserPasswordDTO userPasswordDTO){
        log.info("用户{}申请修改密码",BaseContext.getCurrentUid());
        userInformationService.updateUserPassword(userPasswordDTO);
        log.info("用户{}申请修改密码成功",BaseContext.getCurrentUid());
        return Result.success("用户"+BaseContext.getCurrentUid()+"密码修改成功");
    }

    /**
     * 预先将用户的头像数据传向后端处理成String格式
     */
    @PostMapping("/avatar")
    public Result<String> handleAvatar(MultipartFile avatar) throws IOException {
        log.info("预处理用户图片信息");
        if(avatar==null){
            log.info("图片无法解析或为空");
            return Result.error("图片无法解析或为空");
        }
        log.info("图片预处理成功");
        return Result.success(modifyUserAvatarPathFormatUtil.save(avatar));
    }

}
