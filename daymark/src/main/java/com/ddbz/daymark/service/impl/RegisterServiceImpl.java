package com.ddbz.daymark.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ddbz.daymark.excecption.BusinessException;
import com.ddbz.daymark.mapper.user.RegisterMapper;
import com.ddbz.daymark.pojo.dto.usesr.RegisterDTO;
import com.ddbz.daymark.pojo.entity.user.RegisterEntity;
import com.ddbz.daymark.pojo.vo.user.RegisterDefaultVO;
import com.ddbz.daymark.service.RegisterService;
import com.ddbz.daymark.util.ModifyUserAvatarPathFormatUtil;
import com.ddbz.daymark.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.BindException;
import java.util.Random;

@Slf4j
@Service
public class RegisterServiceImpl implements RegisterService {

    @Autowired
    RegisterMapper registerMapper;

    //导入 BCrtpt 来将密码进行加密
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 根据用户传入数据，来判断用户名是否重复和添加入数据库操作
     * @param registerDTO
     * @return
     */
    @Override
    public Boolean register(RegisterDTO registerDTO) throws IOException {
        log.info("用户{}开始访问数据库",registerDTO.getUid());

        //通过 uid 判断用户名是否重复
        if(judgeUidIsRepeat(registerDTO.getUid())){
            log.info("用户名{}已存在",registerDTO.getUid());
            throw new BusinessException("用户名："+registerDTO.getUid()+"已存在");
        }

        RegisterEntity user = new RegisterEntity();
        user.setAvatar(registerDTO.getAvatar());
        user.setBio(registerDTO.getBio());
        user.setNickname(registerDTO.getNickname());
        user.setUid(registerDTO.getUid());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        registerMapper.insert(user);

        log.info("用户名{}信息成功添加到数据库",registerDTO.getUid());
        return true;
    }



    /**
     * 生成默认用户名和头像
     * @return
     */
    @Value("${app.base-url}")
    private String baseUrl;  //从配置文件中获取baseUrl--存放基础配置地址

    @Override
    public RegisterDefaultVO createDefaultInformal() {
        RegisterDefaultVO registerDefaultVO = new RegisterDefaultVO();
        log.info("成功生成默认用户名");
        registerDefaultVO.setUid(createUsername(5));
        log.info("成功生成默认头像");
        registerDefaultVO.setAvatarUrl(baseUrl+"3cb8738059d1f022db0825072074cd09.jpg");
        return registerDefaultVO;
    }


    /**
     * 生成默认用户名
     * @Param count 如果随机生成的username已经存在，则重新生成，count则为最多重复多少次
     */
    public String createUsername(int RepeatCreateCount){
        int i = 0;
        Random r = new Random();;
        while(i<RepeatCreateCount){
            String uid = r.nextInt(999999999)+""+r.nextInt(999999999);
            //判断username是否重复
            if(!judgeUidIsRepeat(uid)){
                return uid;
            }
            i++;
        }
        return null;

    }


    /**
     * 判断用户名是否重复
     * @param uid
     * @return
     */
    public Boolean judgeUidIsRepeat(String uid){
        LambdaQueryWrapper<RegisterEntity> lqw = new LambdaQueryWrapper<>();
        lqw.eq(RegisterEntity::getUid,uid);
        if(registerMapper.selectOne(lqw)==null){
            return false;
        }
        return true;
    }

}
