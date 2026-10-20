package com.ddbz.daymark.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ddbz.daymark.common.constant.LoginRole;
import com.ddbz.daymark.common.constant.PostStatus;
import com.ddbz.daymark.context.BaseContext;
import com.ddbz.daymark.excecption.BusinessException;
import com.ddbz.daymark.mapper.post.PostMapper;
import com.ddbz.daymark.mapper.reviewer.ReviewMapper;
import com.ddbz.daymark.pojo.dto.post.PostDTO;
import com.ddbz.daymark.pojo.dto.post.PostStatusDTO;
import com.ddbz.daymark.pojo.dto.reviewer.ReviewerDTO;
import com.ddbz.daymark.pojo.entity.post.PostEntity;
import com.ddbz.daymark.pojo.entity.reviewer.ReviewerEntity;
import com.ddbz.daymark.pojo.entity.user.RegisterEntity;
import com.ddbz.daymark.pojo.vo.LoginVO;
import com.ddbz.daymark.pojo.vo.post.PostVO;
import com.ddbz.daymark.service.ReviewerService;
import com.ddbz.daymark.util.JwtUtil;
import com.ddbz.daymark.util.RedisJsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ReviewerServiceImpl implements ReviewerService {

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private RedisJsonUtil redisJsonUtil;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PostMapper postMapper;

    @Value("${reviewer.invite-code}")
    private String inviteCode;


    //导入 BCrtpt 来将密码进行加密
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 注册操作
     * @param reviewerDTO
     */
    @Override
    public void Register(ReviewerDTO reviewerDTO) {
        log.info("先判断邀请码是否正确");
        if(reviewerDTO.getInviteCode()==null||!reviewerDTO.getInviteCode().equals(inviteCode))
            throw new BusinessException("邀请码错误，无法注册管理员");

        log.info("邀请码正确,管理员开始注册操作");

        //判断uid是否重复
        if(judgeUidIsRepeat(reviewerDTO.getUid())){
            throw new BusinessException("uid已经重复");
        }

        log.info("uid独特，开始注册操作");
        ReviewerEntity reviewerEntity = new ReviewerEntity();
        reviewerEntity.setUid(reviewerDTO.getUid());
        reviewerEntity.setPassword(passwordEncoder.encode(reviewerDTO.getPassword()));
        reviewMapper.insert(reviewerEntity);

        redisJsonUtil.set("reviewer:"+reviewerDTO.getUid(),reviewerEntity);
        log.info("数据已存入数据库");
        return;
    }

    /**
     * 审查员登录操作
     * @param reviewerDTO
     * @return
     */
    @Override
    public LoginVO Login(ReviewerDTO reviewerDTO) {
        log.info("开始判断身份合法性,先从缓存中获取审查员数据信息");
        ReviewerEntity reviewerEntity = redisJsonUtil.get("reviewer:"+reviewerDTO.getUid(),ReviewerEntity.class);
        if(reviewerEntity!=null){
            if(judge(reviewerDTO,reviewerEntity)){
                LoginVO login = new LoginVO();
                login.setJwt(jwtUtil.generateToken(reviewerEntity.getUid(),LoginRole.reviewer));
                login.setRole(LoginRole.reviewer);
                return login;
            }else{
                throw new BusinessException("密码或用户名错误");
            }
        }

        log.info("缓存中无管理员数据，向数据库访问");
        LambdaQueryWrapper<ReviewerEntity> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ReviewerEntity::getUid,reviewerDTO.getUid());
        ReviewerEntity reviewer = reviewMapper.selectOne(lqw);
        if(reviewer==null){
            throw new BusinessException("用户名或密码错误");
        }
        redisJsonUtil.set("reviewer:"+reviewerDTO.getUid(),reviewer);
        log.info("数据查找到管理员相关数据，并存到缓存中");
        if(judge(reviewerDTO,reviewer)){
            LoginVO login = new LoginVO();
            login.setJwt(jwtUtil.generateToken(reviewer.getUid(),LoginRole.reviewer));
            login.setRole(LoginRole.reviewer);
            return login;
        }else{
            throw new BusinessException("用户名或密码错误");
        }
    }

    @Override
    public List<PostVO> GetReviewingPost() {
        log.info("开始从缓存中查找待审查的帖子");
        List<PostEntity> postEntities = redisJsonUtil.lRangeAll("reviewingPost",PostEntity.class);
        if(postEntities!=null && !postEntities.isEmpty()){
            List<PostVO> posts = new ArrayList<>();
            for(PostEntity post : postEntities){
                posts.add(getPostVOByPostEntity(post));
            }
            return posts;
        }
        log.info("缓存中无待审核的帖子,开始向数据库申请未审核帖子信息");
        LambdaQueryWrapper<PostEntity> lqw = new LambdaQueryWrapper<>();
        lqw.eq(PostEntity::getStatus, PostStatus.reviewing);
        List<PostEntity> posts = postMapper.selectList(lqw);
        if(posts == null || posts.isEmpty()){
            return new ArrayList<>();
        }
        List<PostVO> newPosts = new ArrayList<>();
        for(PostEntity post : posts){
            newPosts.add(getPostVOByPostEntity(post));
        }
        redisJsonUtil.PushTailAll("reviewingPost",posts);
        log.info("成功获取待审核帖子，并将其存入缓存中");
        return newPosts;
    }

    /**
     * 修改帖子状态
     * @param postStatusDTO
     */
    @Override
    public void UpdatePostStatus(PostStatusDTO postStatusDTO) {
        log.info("开始校验身份");
        if(BaseContext.getCurrentRole()!=LoginRole.reviewer)
            throw new BusinessException("权限不足");

        log.info("开始修改数据库中，帖子的状态信息");
        LambdaQueryWrapper<PostEntity> lqw = new LambdaQueryWrapper<>();
        lqw.eq(PostEntity::getId,postStatusDTO.getId());
        PostEntity postEntity = postMapper.selectOne(lqw);
        if(postEntity==null) throw new BusinessException("数据库无该帖子数据");
        postEntity.setStatus(postStatusDTO.getStatus());
        postMapper.updateById(postEntity);
        log.info("数据库修改成功");

        redisJsonUtil.delete("posts:"+postEntity.getUserId());
        redisJsonUtil.delete("reviewingPost");
        log.info("缓存修改成功");
    }


    /**
     * 判断用户名是否重复
     * @param uid
     * @return
     */
    private Boolean judgeUidIsRepeat(String uid){
        LambdaQueryWrapper<ReviewerEntity> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ReviewerEntity::getUid,uid);
        return reviewMapper.selectOne(lqw) == null;
    }

    /**
     * 将用户数据与审查员数据比对
     */
    private Boolean judge(ReviewerDTO reviewerDTO,ReviewerEntity reviewerEntity){
        if(!reviewerDTO.getUid().equals(reviewerEntity.getUid())){
            return false;
        }

        if(!passwordEncoder.matches(reviewerDTO.getPassword(),reviewerEntity.getPassword())){
            return false;
        }

        log.info("管理员信息比对正确");
        return true;
    }

    private PostVO getPostVOByPostEntity(PostEntity postEntity){
        PostVO post = new PostVO();
        post.setId(postEntity.getId());
        post.setStatus(postEntity.getStatus());
        post.setTitle(postEntity.getTitle());
        post.setUpdatedTime(postEntity.getUpdatedTime());
        post.setContent(postEntity.getContent());
        post.setCreatedTime(postEntity.getCreatedTime());
        post.setUserId(postEntity.getUserId());
        post.setImages(postEntity.getImages());
        return post;
    }

}
