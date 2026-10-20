package com.ddbz.daymark.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ddbz.daymark.context.BaseContext;
import com.ddbz.daymark.excecption.BusinessException;
import com.ddbz.daymark.mapper.user.UserInformationMapper;
import com.ddbz.daymark.pojo.dto.usesr.UserDTO;
import com.ddbz.daymark.pojo.dto.usesr.UserPasswordDTO;
import com.ddbz.daymark.pojo.entity.user.UserEntity;
import com.ddbz.daymark.pojo.vo.user.UserVO;
import com.ddbz.daymark.service.UserInformationService;
import com.ddbz.daymark.util.ModifyUserAvatarPathFormatUtil;
import com.ddbz.daymark.util.RedisJsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.BindException;

@Slf4j
@Service
public class UserInformationServiceImpl implements UserInformationService {
    @Autowired
    private UserInformationMapper userInformationMapper;

    @Autowired
    private ModifyUserAvatarPathFormatUtil modifyUserAvatarPathFormatUtil;

    @Autowired
    private StringRedisTemplate StringRedisTemplate;

    @Autowired
    private RedisJsonUtil redisJsonUtil;


    // JSON编译和反编译工具
    private static final ObjectMapper mapper = new ObjectMapper();

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 获取用户数据
     *
     * @param uid
     * @return
     */
    public UserVO getUserInformation(String uid){
        log.info("正在向缓存申请用户{}个人信息", uid);

        //在缓存当中查询是否有 uid 数据存在
        UserVO user_VO =  redisJsonUtil.get("user:"+uid,UserVO.class);
        if(user_VO!=null){
            return user_VO;
        }

        //数据在缓存中不存在
        log.info("缓存中不存在用户:{}的数据,开始向数据库中访问",BaseContext.getCurrentUid());
        //判断uid是否存在
        LambdaQueryWrapper<UserEntity> lqw = new LambdaQueryWrapper<>();
        lqw.eq(UserEntity::getUid, uid);
        UserEntity user = userInformationMapper.selectOne(lqw);

        //数据库中不存在uid
        if (user == null) {
            throw new BusinessException("不存在用户:"+uid+"的信息");
        }

        UserVO userVO = new UserVO();
        userVO.setNickname(user.getNickname());
        userVO.setAvatar(user.getAvatar());
        userVO.setBio(user.getBio());
        userVO.setUid(user.getUid());
        userVO.setId(user.getId());
        log.info("用户{}成功向数据库申请到个人信息", uid);

        //向缓存中添加数据
        redisJsonUtil.set("user:"+userVO.getUid(),user);
        return userVO;
    }

    /**
     * 修改用户信息
     *
     * @param uid
     * @param userDTO
     */
    @Override
    public void updateUserInformation(String uid, UserDTO userDTO) throws IOException {
        log.info("用户{}开始向数据库申请修改个人数据", uid);


        //判断uid是否存在
        LambdaQueryWrapper<UserEntity> lqw = new LambdaQueryWrapper<>();
        lqw.eq(UserEntity::getUid, uid);
        UserEntity user = userInformationMapper.selectOne(lqw);
        if (user == null) {
            log.info("用户{}不存在,用户信息修改失败", uid);
            throw new BusinessException("用户"+uid+"不存在，信息修改失败");
        }
        UserEntity newUser = new UserEntity();
        newUser.setPassword(user.getPassword());
        newUser.setUid(user.getUid());

        if(userDTO.getNickname() != null) {
            newUser.setNickname(userDTO.getNickname());
        }else{
            newUser.setNickname(user.getNickname());
        }

        if(userDTO.getBio() != null) {
            newUser.setBio(userDTO.getBio());
        }else{
            newUser.setBio(user.getBio());
        }

        // avatar 已是 URL 字符串（由 /user/me/avatar 接口预先上传得到），直接存入
        if(userDTO.getAvatar() != null && !userDTO.getAvatar().isEmpty()) {
            newUser.setAvatar(userDTO.getAvatar());
        }else{
            newUser.setAvatar(user.getAvatar());
        }

        userInformationMapper.update(newUser,lqw);

        //修改数据库后，要删除缓存
        StringRedisTemplate.delete("user:"+uid);

        log.info("用户{}信息修改成功", uid);
        return;
    }


    /**
     * 修改用户密码
     * @param userPasswordDTO
     * @return
     */
    @Override
    public void updateUserPassword(UserPasswordDTO userPasswordDTO) {
        log.info("开始比对用户{}密码正确性", BaseContext.getCurrentUid());

        //访问数据库，获取用户信息
        LambdaQueryWrapper<UserEntity> lqw = new LambdaQueryWrapper<>();
        lqw.eq(UserEntity::getUid,BaseContext.getCurrentUid());
        UserEntity user = userInformationMapper.selectOne(lqw);

        //通过Bcrypt来比对密码正确性，并编码新密码，存入数据库当中
        if(passwordEncoder.matches(userPasswordDTO.getPassword(),user.getPassword())){
            log.info("用户{}密码正确，开始修改密码",BaseContext.getCurrentUid());
            user.setPassword(passwordEncoder.encode(userPasswordDTO.getNewPassword()));
            userInformationMapper.update(user,lqw);
            log.info("用户{}密码修改成功",BaseContext.getCurrentUid());
            redisJsonUtil.delete("user:"+user.getUid());
            return;
        }

        throw new BusinessException("用户:"+user.getUid()+"输入密码错误，禁止修改密码");
    }

}
