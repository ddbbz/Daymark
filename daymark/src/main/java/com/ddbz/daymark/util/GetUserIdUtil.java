package com.ddbz.daymark.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ddbz.daymark.context.BaseContext;
import com.ddbz.daymark.mapper.user.UserInformationMapper;
import com.ddbz.daymark.pojo.entity.user.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GetUserIdUtil {

    @Autowired
    private UserInformationMapper userMapper;

    public int get(){
        LambdaQueryWrapper<UserEntity> lqw = new LambdaQueryWrapper<>();
        lqw.eq(UserEntity::getUid, BaseContext.getCurrentUid());
        return userMapper.selectOne(lqw).getId();
    }
}
