package com.ddbz.daymark.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ddbz.daymark.context.BaseContext;
import com.ddbz.daymark.excecption.BusinessException;
import com.ddbz.daymark.mapper.function.CommentMapper;
import com.ddbz.daymark.mapper.post.PostMapper;
import com.ddbz.daymark.pojo.dto.function.CommentDTO;
import com.ddbz.daymark.pojo.entity.function.CommentEntity;
import com.ddbz.daymark.pojo.entity.post.PostEntity;
import com.ddbz.daymark.pojo.vo.function.CommentVO;
import com.ddbz.daymark.service.CommentService;
import com.ddbz.daymark.service.PostInformationService;
import com.ddbz.daymark.util.RedisJsonUtil;
import com.ddbz.daymark.util.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserUtil userUtil;

    @Autowired
    private RedisJsonUtil redisJsonUtil;

    @Autowired
    private PostMapper postMapper;

    /**
     * 创建评论
     * @param commentDTO
     */
    @Override
    public void Comment(CommentDTO commentDTO) {
        if(commentDTO.getContent()==null){
            throw new BusinessException("评论内容为空");
        }
        CommentEntity comment = getEntityByDTO(commentDTO);
        commentMapper.insert(comment);
        updatePostCommentCount(commentDTO,true);
        redisJsonUtil.delete("comment:"+comment.getPostId());
    }


    /**
     * 删除评论操作
     * @param commentDTO
     */
    @Override
    public void DeleteComment(CommentDTO commentDTO) {
        CommentEntity commentEntity = commentMapper.selectById(commentDTO.getId());
        if(commentEntity.getUserId()!=commentDTO.getUserId())
            throw new BusinessException("不可以删除别人的帖子");
        CommentEntity comment = getEntityByDTO(commentDTO);
        commentMapper.deleteById(comment);
        redisJsonUtil.delete("comment:"+comment.getId());
        updatePostCommentCount(commentDTO,false);
    }


    /**
     * 获取评论信息
     * @param postId
     * @return
     */
    @Override
    public List<CommentVO> GetComment(int postId) {
        log.info("从缓存中获取评论信息");
        List<CommentEntity> comments = redisJsonUtil.lRangeAll("comment:"+postId,CommentEntity.class);
        if(comments!=null && !comments.isEmpty()){
            log.info("成功从缓存中获取评论数据");
            List<CommentVO> newComments = new ArrayList<>();
            for(CommentEntity comment : comments){
                newComments.add(getVOByEntity(comment));
            }
            return newComments;
        }

        log.info("缓存中无用户数据，向数据库申请评论数据");
        LambdaQueryWrapper<CommentEntity> lqw = new LambdaQueryWrapper<>();
        lqw.eq(CommentEntity::getPostId,postId);
        List<CommentEntity> commentEntities = commentMapper.selectList(lqw);
        if(commentEntities.isEmpty())
            return new ArrayList<>();
        List<CommentVO> newComments = new ArrayList<>();
        for(CommentEntity comment : commentEntities){
            newComments.add(getVOByEntity(comment));
        }

        redisJsonUtil.PushTailAll("comment:"+postId,commentEntities);
        log.info("从数据库获取，并缓存成功");
        return newComments;
    }

    //将DTO转换为Entity
    private CommentEntity getEntityByDTO(CommentDTO commentDTO){
        log.info("开始解析评论格式");
        CommentEntity comment = new CommentEntity();
        comment.setCommentObjectIsPost(commentDTO.isCommentObjectIsPost());
        comment.setContent(commentDTO.getContent());
        comment.setUserId(userUtil.getUserIdByUid(BaseContext.getCurrentUid()));
        comment.setPostId(commentDTO.getPostId());
        comment.setId(commentDTO.getId());
        log.info("解析成功");
        return comment;
    }

    //将Entity转换为VO
    private CommentVO getVOByEntity(CommentEntity commentEntity){
        log.info("开始打包评论信息");
        CommentVO comment = new CommentVO();
        comment.setCommentObjectIsPost(commentEntity.isCommentObjectIsPost());
        comment.setContent(commentEntity.getContent());
        comment.setUserId(commentEntity.getUserId());
        comment.setPostId(commentEntity.getPostId());
        comment.setCreatedTime(commentEntity.getCreatedTime());
        comment.setId(commentEntity.getId());
        log.info("打包成功");
        return comment;
    }

    //修改帖子评论数
    private void updatePostCommentCount(CommentDTO commentDTO,boolean add){
        log.info("开始修改帖子评论数");
        LambdaQueryWrapper<PostEntity> lqw = new LambdaQueryWrapper<>();
        lqw.eq(PostEntity::getId,commentDTO.getPostId());
        PostEntity post= postMapper.selectOne(lqw);
        if(post==null)
            throw new RuntimeException("帖子未知");
        if(add){
            post.setCommentCount(post.getCommentCount() + 1);
            log.info("帖子数成功加一");
        }else{
            if(post.getCommentCount()==0){
                throw new BusinessException("该帖子评论数量为空，删除失败");
            }else {
                post.setCommentCount(post.getCommentCount() - 1);
                log.info("帖子数成功减一");
            }
        }
        postMapper.updateById(post);
        redisJsonUtil.delete("posts:"+post.getUserId());
    }
}
