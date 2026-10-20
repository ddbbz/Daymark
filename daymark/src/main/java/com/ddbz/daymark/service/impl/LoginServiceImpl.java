package com.ddbz.daymark.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ddbz.daymark.common.constant.LoginRole;
import com.ddbz.daymark.excecption.BusinessException;
import com.ddbz.daymark.mapper.user.LoginMapper;
import com.ddbz.daymark.pojo.dto.usesr.LoginDTO;
import com.ddbz.daymark.pojo.entity.user.LoginEntity;
import com.ddbz.daymark.pojo.vo.LoginVO;
import com.ddbz.daymark.service.LoginService;
import com.ddbz.daymark.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LoginServiceImpl implements LoginService {

    @Autowired
    private LoginMapper loginMapper;

    @Autowired
    private JwtUtil jwtUtil;


    //导入 BCrypt 来进行对密码进行安全比较
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public LoginVO judgeByUserNameAndPassword(LoginDTO loginDTO) {
        log.info("用户{}开始校验密码正确性", loginDTO.getUid());

        LambdaQueryWrapper<LoginEntity> lqw = new LambdaQueryWrapper<>();
        //判断用户是否存在
        lqw.eq(LoginEntity::getUid, loginDTO.getUid());
        LoginEntity loginEntity = loginMapper.selectOne(lqw);
        if (loginEntity == null) {
            log.info("用户{}不存在", loginDTO.getUid());
            throw new BusinessException("用户："+loginDTO.getUid()+"名称不存在");
        }


        //判断密码是否正确
        if (!passwordEncoder.matches(loginDTO.getPassword(), loginEntity.getPassword())) {
            log.info("用户:{}密码校验完成", loginDTO.getUid());
            throw new BusinessException("用户名或密码错误");
        }

        log.info("用户{}密码校验完成", loginDTO.getUid());

        LoginVO login = new LoginVO();
        login.setJwt(jwtUtil.generateToken(loginEntity.getUid(),LoginRole.user));
        login.setRole(LoginRole.user);
        return login;
    }
}
