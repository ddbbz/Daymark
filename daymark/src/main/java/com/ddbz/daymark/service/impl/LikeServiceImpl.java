package com.ddbz.daymark.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ddbz.daymark.context.BaseContext;
import com.ddbz.daymark.excecption.BusinessException;
import com.ddbz.daymark.mapper.function.LikeMapper;
import com.ddbz.daymark.mapper.post.PostMapper;
import com.ddbz.daymark.mapper.user.UserInformationMapper;
import com.ddbz.daymark.pojo.dto.function.LikeDTO;
import com.ddbz.daymark.pojo.entity.function.LikeEntity;
import com.ddbz.daymark.pojo.entity.post.PostEntity;
import com.ddbz.daymark.pojo.entity.user.UserEntity;
import com.ddbz.daymark.pojo.vo.function.LikeVO;
import com.ddbz.daymark.service.LikeService;
import com.ddbz.daymark.util.GetUserIdUtil;
import com.ddbz.daymark.util.RedisJsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class LikeServiceImpl implements LikeService {

    @Autowired
    private RedisJsonUtil redisJsonUtil;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private LikeMapper likeMapper;

    @Autowired
    private GetUserIdUtil getUserIdUtil;

    /**
     * 点赞操作
     * @param postId
     */
    @Override
    public void Like(int postId) {
        log.info("开始修改与点赞相关的数据操作");

        LambdaQueryWrapper<LikeEntity> lqw = new LambdaQueryWrapper<>();
        lqw.eq(LikeEntity::getPostId,postId).eq(LikeEntity::getUserId,getUserIdUtil.get());
        if(likeMapper.selectOne(lqw) != null) throw  new BusinessException("这名用户已经点过赞了");

        LikeEntity like = new LikeEntity();
        like.setPostId(postId);
        like.setUserId(getUserIdUtil.get());
        updateLikeCount(postId, true);
        likeMapper.insert(like);
        redisJsonUtil.delete("like:"+postId);
        log.info("操作成功");
    }

    /**
     * 取消点赞操作
     * @param postId
     */
    @Override
    public void DeleteLike(int postId) {
        log.info("开始向数据库删除点赞信息");
        int userId = getUserIdUtil.get();
        LambdaQueryWrapper<LikeEntity> lqw = new LambdaQueryWrapper<>();
        lqw.eq(LikeEntity::getPostId,postId).eq(LikeEntity::getUserId,userId);
        LikeEntity like = likeMapper.selectOne(lqw);
        if(like == null) throw new BusinessException("用户未点赞，取消点赞失败");
        updateLikeCount(postId,false);
        likeMapper.deleteById(like);
        redisJsonUtil.delete("like:"+postId);
        log.info("数据库和缓存删除点赞信息成功");
    }

    @Override
    public List<LikeVO> LikeInformation(int postId) {
        int userId = getUserIdUtil.get();
        log.info("向缓存查找该点赞信息");
        List<LikeEntity> likes = redisJsonUtil.lRangeAll("like:"+postId,LikeEntity.class);
        if(likes!=null){
            List<LikeVO> newLikes = new ArrayList<>();
            for(LikeEntity like : likes) {
                newLikes.add(getVOByEntity(like));
            }
            return newLikes;
        }

        log.info("缓存中无该点赞信息，开始向数据库发送请求");
        LambdaQueryWrapper<LikeEntity> lqw = new LambdaQueryWrapper<>();
        lqw.eq(LikeEntity::getPostId,postId);
        List<LikeEntity> likeEntities = likeMapper.selectList(lqw);
        List<LikeVO> newLikes = new ArrayList<>();
        for(LikeEntity like : likeEntities){
            newLikes.add(getVOByEntity(like));
        }
        redisJsonUtil.PushTailAll("like:"+postId,likeEntities);
        return newLikes;
    }

    /**
     * 判断用户有无点赞
     * @param postId
     * @return
     */
    @Override
    public Boolean IsLike(int postId) {
        log.info("判断缓存中有无点赞信息");
        List<LikeEntity> likes = redisJsonUtil.lRangeAll("like:"+postId,LikeEntity.class);
        if(likes!=null && !likes.isEmpty()){
            return isLike(likes);
        }
        log.info("缓存中无该用户信息,向数据申请");
        LambdaQueryWrapper<LikeEntity> lqw = new LambdaQueryWrapper<>();
        lqw.eq(LikeEntity::getPostId,postId);
        List<LikeEntity> likeEntities = likeMapper.selectList(lqw);
        redisJsonUtil.PushTailAll("like:"+postId,likeEntities);
        return isLike(likeEntities);
    }

    //修改帖子的点赞数量
    private void updateLikeCount(int postId,boolean isAdd){
        LambdaQueryWrapper<PostEntity> lqw = new LambdaQueryWrapper<>();
        lqw.eq(PostEntity::getId,postId);
        PostEntity post = postMapper.selectOne(lqw);
        if(post==null){
            throw new BusinessException("该帖子不存在");
        }
        if(isAdd){
            post.setLikeCount(post.getLikeCount()+1);
        }else{
            if(post.getLikeCount()==0){
                throw new BusinessException("帖子点赞数为空");
            }else {
                post.setLikeCount(post.getLikeCount()-1);
            }
        }
        postMapper.updateById(post);
        redisJsonUtil.delete("posts:"+post.getUserId());
    }

    //将Entity转换为VO
    private LikeVO getVOByEntity(LikeEntity likeEntity){
        LikeVO like = new LikeVO();
        like.setId(likeEntity.getId());
        like.setCreatedTime(likeEntity.getCreatedTime());
        like.setPostId(likeEntity.getPostId());
        like.setUserId(likeEntity.getUserId());
        return like;
    }

    //根据UserId判断有无点赞
    private Boolean isLike(List<LikeEntity> likes){
        int userId = getUserIdUtil.get();
        for(LikeEntity like : likes){
            if(like.getUserId()== userId){
                return true;
            }
        }
        return false;
    }
}
