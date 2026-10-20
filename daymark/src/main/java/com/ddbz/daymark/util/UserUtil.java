package com.ddbz.daymark.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ddbz.daymark.mapper.user.UserInformationMapper;
import com.ddbz.daymark.pojo.entity.user.UserEntity;
import com.ddbz.daymark.pojo.vo.user.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserUtil {

    @Autowired
    RedisJsonUtil redisJsonUtil;

    @Autowired
    UserInformationMapper userInformationMapper;

    //根据uid获取用户信息
    public int getUserIdByUid(String uid){

        UserEntity user = redisJsonUtil.get("user:"+uid, UserEntity.class);
        if(user != null){
            return user.getId();
        }

        log.info("缓存中，无用户:{}的数据，开始向数据库中查询",uid);
        LambdaQueryWrapper<UserEntity> lqw = new LambdaQueryWrapper<>();
        lqw.eq(UserEntity::getUid,uid);
        UserEntity userEntity = userInformationMapper.selectOne(lqw);

        redisJsonUtil.set("user:"+uid,userEntity);

        log.info("查找用户信息完成");
        return userEntity.getId();
    }
}
