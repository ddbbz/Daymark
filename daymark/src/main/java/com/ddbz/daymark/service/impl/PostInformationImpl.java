package com.ddbz.daymark.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ddbz.daymark.context.BaseContext;
import com.ddbz.daymark.common.constant.PostStatus;
import com.ddbz.daymark.excecption.BusinessException;
import com.ddbz.daymark.mapper.user.UserInformationMapper;
import com.ddbz.daymark.mapper.post.PostMapper;
import com.ddbz.daymark.pojo.dto.post.PostDTO;
import com.ddbz.daymark.pojo.dto.post.PostStatusDTO;
import com.ddbz.daymark.pojo.entity.user.UserEntity;
import com.ddbz.daymark.pojo.entity.post.PostEntity;
import com.ddbz.daymark.pojo.vo.post.PostVO;
import com.ddbz.daymark.pojo.vo.user.UserVO;
import com.ddbz.daymark.service.PostInformationService;
import com.ddbz.daymark.util.GetUserIdUtil;
import com.ddbz.daymark.util.ModifyUserAvatarPathFormatUtil;
import com.ddbz.daymark.util.RedisJsonUtil;
import com.ddbz.daymark.util.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PostInformationImpl implements PostInformationService {


    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserInformationMapper userInformationMapper;

    @Autowired
    private ModifyUserAvatarPathFormatUtil modifyUserAvatarPathFormatUtil;

    @Autowired
    private RedisJsonUtil redisJsonUtil;

    @Autowired
    private UserUtil userUtil;

    @Autowired
    private GetUserIdUtil getUserIdUtil;


    /**
     * 创建帖子
     *
     * @param postDTO
     */
    @Override
    public void publishPost(PostDTO postDTO) throws IOException {
        log.info("开始创键用户{}的帖子", BaseContext.getCurrentUid());

        //判断帖子是否为空
        if(postDTO.getContent()==null
                && postDTO.getTitle()==null
                && postDTO.getImage()==null){
            log.info("用户{}的帖子为空，创建失败",BaseContext.getCurrentUid());
            throw new BusinessException("用户:"+BaseContext.getCurrentUid()+"的帖子为空，创建失败");
        }

        //开始向往entity中传递参数
        PostEntity postEntity = new PostEntity();
        postEntity.setTitle(postDTO.getTitle());
        postEntity.setContent(postDTO.getContent());
        postEntity.setStatus(PostStatus.reviewing);  //为帖子状态设置为“正在审核中”
        postEntity.setImages(postDTO.getImage());

        //根据uid获得用户的id
        postEntity.setUserId(getUserIdByUid(BaseContext.getCurrentUid()));
        log.info("id校验成功");


        //将帖子存入数据库
        postMapper.insert(postEntity);
        log.info("用户{}数据成功存入数据库",BaseContext.getCurrentUid());

        //将帖子存入缓存
        redisJsonUtil.PushTail("posts:"+postEntity.getUserId(),postEntity);

        log.info("用户{}创建帖子操作成功，等待审核",BaseContext.getCurrentUid());

        redisJsonUtil.delete("reviewingPost");
    }

    /**
     *根据用户 uid找其帖子
     * @param uid
     * @return
     */
    @Override
    public List<PostVO> showByUid(String uid) {
        if(uid == null) return new ArrayList<>();

        int userId = getUserIdByUid(uid);

        if(userId == Integer.MAX_VALUE){
            log.info("uid为{}的用户，暂无发布帖子",uid);
            return new ArrayList<>();
        }

        return EntityConvertVO(getPostsByUserId(userId));
    }


    /**
     * 根据用户 nickname找其帖子
     * @param nickname
     * @return
     */
    @Override
    public List<PostVO> showByNickname(String nickname) {
        log.info("开始根据用户nickname:{}查询用户uid",nickname);
        LambdaQueryWrapper<UserEntity> lqw = new LambdaQueryWrapper<>();
        lqw.eq(UserEntity::getNickname,nickname);
        List<UserEntity> users = userInformationMapper.selectList(lqw);
        List<PostVO> result = new ArrayList<>();
        for(UserEntity user : users) {
            List<PostVO> posts =EntityConvertVO(getPostsByUserId(user.getId()));
            result.addAll(posts);
        }
        return result;
    }

    @Override
    public List<PostVO> showByTitle(String title) {
        log.info("开始根据标题信息模糊查询帖子");
        LambdaQueryWrapper<PostEntity> lqw = new LambdaQueryWrapper<>();
        lqw.like(PostEntity::getTitle,title)              //模糊搜索
                .eq(PostEntity::getStatus,PostStatus.success)         //要审核通过的帖子
                .orderByDesc(PostEntity::getCreatedTime);         //新发布的优先
        List<PostEntity> posts = postMapper.selectList(lqw);
        if(posts==null || posts.isEmpty()){
            return new ArrayList<>();
        }
        return EntityConvertVO(posts);
    }

    /**
     * 根据帖子信息，修改帖子状态
     * @param postStatusDTO
     */
    @Override
    public void updatePostStatus(PostStatusDTO postStatusDTO) {
        log.info("开始向数据库申请修改帖子状态");
        LambdaQueryWrapper<PostEntity> lwq = new LambdaQueryWrapper<>();
        lwq.eq(PostEntity::getId,postStatusDTO.getId());
        PostEntity post = postMapper.selectOne(lwq);
        if(post == null){
            throw new BusinessException("用户名不存在");
        }
        String uid = getUidByUserId(post.getUserId());
        if(uid==null || !uid.equals(BaseContext.getCurrentUid())){
            throw new BusinessException("用户与帖子用户不一致，禁止修改");
        }
        post.setStatus(postStatusDTO.getStatus());
        postMapper.updateById(post);
        log.info("数据库修改信息成功");
        redisJsonUtil.delete("posts:"+post.getUserId());
        redisJsonUtil.delete("reviewingPost");
    }

    /**
     * 实现帖子修改操作
     * @param postDTO
     */
    @Override
    public void updatePost(PostDTO postDTO) {
        //判断帖子是否为空
        if(postDTO.getContent()==null
                && postDTO.getTitle()==null
                && postDTO.getImage()==null){
            log.info("用户{}的帖子为空，修改失败",BaseContext.getCurrentUid());
            throw new BusinessException("用户:"+BaseContext.getCurrentUid()+"的帖子为空，修改失败");
        }

        PostEntity oldPost = postMapper.selectById(postDTO.getId());
        if(oldPost==null) throw new BusinessException("数据库无该帖子的信息");
        int userId = postDTO.getUserId();
        if(userId!=getUserIdUtil.get()){
            throw new BusinessException("要修改的用户与帖子作者不匹配");
        }

        //开始向往entity中传递参数
        PostEntity postEntity = new PostEntity();
        postEntity.setTitle(postDTO.getTitle());
        postEntity.setContent(postDTO.getContent());
        postEntity.setStatus(PostStatus.reviewing);  //为帖子状态设置为“正在审核中”
        postEntity.setImages(postDTO.getImage());
        postEntity.setId(postDTO.getId());

        //根据uid获得用户的id
        postEntity.setUserId(getUserIdByUid(BaseContext.getCurrentUid()));
        log.info("id校验成功");


        //将帖子存入数据库
        postMapper.updateById(postEntity);
        log.info("用户{}帖子成功在数据库中修改",BaseContext.getCurrentUid());

        //将帖子存入缓存
        redisJsonUtil.PushTail("posts:"+postEntity.getUserId(),postEntity);
        redisJsonUtil.delete("posts:"+postEntity.getUserId());

        log.info("用户{}创建帖子操作成功，等待审核",BaseContext.getCurrentUid());
    }


    //从 Entity 提取出 VO 所需要的信息
    private List<PostVO> EntityConvertVO(List<PostEntity> postsEntity){
        List<PostVO> posts = new ArrayList<>();
        for(PostEntity postEntity:postsEntity) {
            PostVO postVO = new PostVO();
            postVO.setCommentCount(postEntity.getCommentCount());
            postVO.setCreatedTime(postEntity.getCreatedTime());
            postVO.setContent(postEntity.getContent());
            postVO.setImages(postEntity.getImages());
            postVO.setFavoriteCount(postEntity.getFavoriteCount());
            postVO.setLikeCount(postEntity.getLikeCount());
            postVO.setUpdatedTime(postEntity.getUpdatedTime());
            postVO.setStatus(postEntity.getStatus());
            postVO.setTitle(postEntity.getTitle());
            postVO.setId(postEntity.getId());
            postVO.setUserId(postEntity.getUserId());
            posts.add(postVO);
        }
        return posts;
    }

    //根据uid获取用户信息
    private int getUserIdByUid(String uid){

        log.info("开始根据用户{},向缓存中查找用户信息",uid);
        UserEntity user = redisJsonUtil.get("user:"+uid, UserEntity.class);
        if(user != null){
            return user.getId();
        }

        log.info("缓存中，无用户:{}的数据，开始向数据库中查询",uid);
        LambdaQueryWrapper<UserEntity> lqw = new LambdaQueryWrapper<>();
        lqw.eq(UserEntity::getUid,uid);
        UserEntity userEntity = userInformationMapper.selectOne(lqw);

        if(userEntity==null){
            return Integer.MAX_VALUE;
        }

        redisJsonUtil.set("user:"+uid,userEntity);

        log.info("查找用户信息完成");
        return userEntity.getId();
    }

    //根据用户id来获取其帖子
    private List<PostEntity> getPostsByUserId(int userId){
        log.info("开始向缓存中查找用户的帖子");
        //查看缓存中，是否存在该用户的帖子
        List<PostEntity> posts = redisJsonUtil.lRangeAll("posts:"+userId,PostEntity.class);
        if(posts != null && !posts.isEmpty()){
            return posts;
        }

        log.info("暂无从缓存中找到用户:{}的帖子,开始向数据库查找",userId);
        LambdaQueryWrapper<PostEntity> lqw = new LambdaQueryWrapper<>();
        lqw.eq(PostEntity::getUserId,userId).eq(PostEntity::getStatus,PostStatus.success);
        List<PostEntity> post = postMapper.selectList(lqw);
        redisJsonUtil.PushTailAll("posts:"+userId,post);
        return post;
    }

    //根据id获得uid
    private String getUidByUserId(int id){
        LambdaQueryWrapper<UserEntity> lqw = new LambdaQueryWrapper<>();
        lqw.eq(UserEntity::getId,id);
        UserEntity user = userInformationMapper.selectOne(lqw);
        if(user == null)
            throw new BusinessException("用户不存在");
        return user.getUid();
    }

    //
}
