package com.ddbz.daymark.controller.User;


import com.ddbz.daymark.common.Result;
import com.ddbz.daymark.pojo.dto.usesr.RegisterDTO;
import com.ddbz.daymark.pojo.vo.user.RegisterDefaultVO;
import com.ddbz.daymark.service.RegisterService;
import com.ddbz.daymark.util.ModifyUserAvatarPathFormatUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


/**
 * 注册操作
 */

@RequestMapping("/register")
@Slf4j
@RestController
public class RegisterController {

    @Autowired
    private RegisterService registerService;

    /**
     * 根据前端提供的数据来注册用户
     * @param registerDTO
     * @return
     */

    @PostMapping()
    public Result<String> register(@RequestBody RegisterDTO registerDTO) throws IOException {
        log.info("用户{}注册操作开始",registerDTO.getUid());
        registerService.register(registerDTO);
        log.info("用户{}注册成功",registerDTO.getUid());
        return Result.success("注册成功");
    }

    @GetMapping
    public Result<RegisterDefaultVO> getDefaultUidAndAvatar(){
        log.info("新用户申请传递默认uid和头像");
        RegisterDefaultVO rdVO = registerService.createDefaultInformal();
        log.info("新用户成功申请到默认的uid和头像");
        return Result.success(rdVO);
    }



    @Autowired
    ModifyUserAvatarPathFormatUtil modifyUserAvatarPathFormatUtil;

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
