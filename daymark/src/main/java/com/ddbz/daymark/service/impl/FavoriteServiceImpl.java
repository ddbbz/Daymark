package com.ddbz.daymark.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ddbz.daymark.excecption.BusinessException;
import com.ddbz.daymark.mapper.function.FavoriteMapper;
import com.ddbz.daymark.mapper.post.PostMapper;
import com.ddbz.daymark.pojo.entity.function.FavoriteEntity;
import com.ddbz.daymark.pojo.entity.post.PostEntity;
import com.ddbz.daymark.pojo.vo.function.FavoriteVO;
import com.ddbz.daymark.service.FavoriteServiice;
import com.ddbz.daymark.util.GetUserIdUtil;
import com.ddbz.daymark.util.RedisJsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class FavoriteServiceImpl implements FavoriteServiice {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private GetUserIdUtil getUserIdUtil;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private RedisJsonUtil redisJsonUtil;

    /**
     * 收藏
     * @param postId
     */
    @Override
    public void Favorite(int postId) {
        log.info("将用户收藏的数据添加到数据库中");

        LambdaQueryWrapper<FavoriteEntity> lqw = new LambdaQueryWrapper();
        lqw.eq(FavoriteEntity::getUserId,getUserIdUtil.get()).eq(FavoriteEntity::getPostId,postId);
        if(favoriteMapper.selectOne(lqw)!=null) throw new BusinessException("该帖子已收藏");

        FavoriteEntity favorite = new FavoriteEntity();
        favorite.setPostId(postId);
        favorite.setUserId(getUserIdUtil.get());
        favoriteMapper.insert(favorite);
        log.info("收藏添加成功");
        updatePostFavorite(postId,true);
        log.info("帖子收藏数信息修改成功");
        redisJsonUtil.delete("favorite:"+postId);
    }

    /**
     * 取消收藏
     * @param postId
     */
    @Override
    public void DeleteFavorite(int postId) {
        LambdaQueryWrapper<FavoriteEntity> lqw = new LambdaQueryWrapper();
        lqw.eq(FavoriteEntity::getPostId,postId).eq(FavoriteEntity::getUserId,getUserIdUtil.get());
        FavoriteEntity favorite = favoriteMapper.selectOne(lqw);
        if(favorite == null){
            return;
        }
        favoriteMapper.deleteById(favorite);
        redisJsonUtil.delete("favorite:"+postId);
        log.info("收藏取消成功");
        updatePostFavorite(postId,false);
        log.info("帖子收藏数信息修改成功");
    }

    /**
     * 判断用户有没有收藏该帖子
     * @param postId
     * @return
     */
    @Override
    public Boolean IsFavorite(int postId) {
        log.info("开始判断用户是否收藏了该帖子");

        List<FavoriteEntity> fes = redisJsonUtil.lRangeAll("favorite:"+postId,FavoriteEntity.class);
        if(fes!=null){
            for(FavoriteEntity fe :fes){
                if(fe.getUserId()== getUserIdUtil.get()){
                    return true;
                }
            }
        }

        LambdaQueryWrapper<FavoriteEntity> lqw = new LambdaQueryWrapper<>();
        lqw.eq(FavoriteEntity::getUserId,getUserIdUtil.get()).eq(FavoriteEntity::getPostId,postId);
        if(favoriteMapper.selectOne(lqw)==null){
            log.info("用户未收藏该帖子");
            return false;
        }
        log.info("用户已收藏该帖子");
        return true;
    }

    /**
     * 获取帖子收藏信息
     * @param postId
     * @return
     */
    @Override
    public List<FavoriteVO> FavoriteInformation(int postId) {
        log.info("开始向缓存获取帖子的收藏数据");
        List<FavoriteEntity> favorites = redisJsonUtil.lRangeAll("favorite:"+postId,FavoriteEntity.class);
        if(favorites!=null && !favorites.isEmpty()){
            List<FavoriteVO> favoriteVOS = new ArrayList<>();
            for(FavoriteEntity favoriteEntity:favorites){
                favoriteVOS.add(getVOByEntity(favoriteEntity));
            }
            return favoriteVOS;
        }

        log.info("缓存中无存储该帖子收藏数据，开始向数据库寻求");
        LambdaQueryWrapper<FavoriteEntity> lqw = new LambdaQueryWrapper<>();
        lqw.eq(FavoriteEntity::getPostId,postId);
        List<FavoriteEntity> favoriteEntities = favoriteMapper.selectList(lqw);
        List<FavoriteVO> newFavorite = new ArrayList<>();
        for(FavoriteEntity favorite:favoriteEntities){
            newFavorite.add(getVOByEntity(favorite));
        }

        redisJsonUtil.PushTailAll("favorite:"+postId,favoriteEntities);
        log.info("成功从数据库中获取数据，并存入缓存中");
        return newFavorite;
    }

    private void updatePostFavorite(int postId,Boolean isAdd){
        LambdaQueryWrapper<PostEntity> lqw = new LambdaQueryWrapper<>();
        lqw.eq(PostEntity::getId,postId);
        PostEntity post = postMapper.selectOne(lqw);
        if(post==null) throw new BusinessException("没有该帖子");
        if(isAdd){
            post.setFavoriteCount(post.getFavoriteCount()+1);
        }else{
            if(post.getFavoriteCount()==0){
                throw new BusinessException("该帖子无收藏，收藏取消失败");
            }else{
                post.setFavoriteCount(post.getFavoriteCount()-1);
            }
        }
        postMapper.updateById(post);
        redisJsonUtil.delete("posts:"+post.getUserId());
    }

    private FavoriteVO getVOByEntity(FavoriteEntity favoriteEntity){
        FavoriteVO favoriteVO = new FavoriteVO();
        favoriteVO.setId(favoriteEntity.getId());
        favoriteVO.setPostId(favoriteEntity.getPostId());
        favoriteVO.setUserId(favoriteEntity.getUserId());
        favoriteVO.setCreatedTime(favoriteEntity.getCreatedTime());
        return favoriteVO;
    }
}
