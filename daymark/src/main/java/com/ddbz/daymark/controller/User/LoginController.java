package com.ddbz.daymark.controller.User;

import com.ddbz.daymark.common.Result;
import com.ddbz.daymark.pojo.dto.usesr.LoginDTO;
import com.ddbz.daymark.pojo.vo.LoginVO;
import com.ddbz.daymark.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/login")
@RestController
@Slf4j
/**
 * 登录操作
 */
public class LoginController {

    @Autowired
    private LoginService loginService;


    /**
     * 登录并验证密码与用户名是否匹配
     */
    @PostMapping
    public Result<LoginVO> judgeByUserNameAndPassword(@RequestBody LoginDTO loginDTO) {
        log.info("用户{}登陆操作开始",loginDTO.getUid());

        LoginVO login = loginService.judgeByUserNameAndPassword(loginDTO);

        log.info("用户{}登陆操作成功",loginDTO.getUid());
        return Result.success(login);
    }

}