package com.ddbz.daymark.controller.post;

import com.ddbz.daymark.common.constant.PostStatus;
import com.ddbz.daymark.context.BaseContext;
import com.ddbz.daymark.common.Result;
import com.ddbz.daymark.pojo.dto.post.PostDTO;
import com.ddbz.daymark.pojo.dto.post.PostStatusDTO;
import com.ddbz.daymark.pojo.vo.post.PostVO;
import com.ddbz.daymark.service.PostInformationService;
import com.ddbz.daymark.util.ModifyUserAvatarPathFormatUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 帖子发布
 */
@RestController
@Slf4j
@RequestMapping("/post")
public class PostController {

    @Autowired
    private PostInformationService postInformationService;

    @Autowired
    ModifyUserAvatarPathFormatUtil modifyUserAvatarPathFormatUtil;


    /**
     * 帖子的发布
     */
    @PostMapping("/publish")
    public Result<String> publishPost(@RequestBody PostDTO postDTO) throws IOException {
            log.info("用户{}申请创建帖子", BaseContext.getCurrentUid());
            postInformationService.publishPost(postDTO);
            log.info("用户{}的帖子申请成功，正在审核中", BaseContext.getCurrentUid());
            return Result.success("帖子创建成功，请等待审核");
    }

    /**
     * 根据作者uid查找帖子
     * @param uid
     * @return
     */
    @GetMapping("/showByUid")
    public Result<List<PostVO>> showPostByUid(@RequestParam String uid){
        log.info("访问用户{}的帖子",uid);
        return Result.success(postInformationService.showByUid(uid));
    }

    /**
     * 根据用户名称查找帖子
     * @param nickname
     * @return
     */
    @GetMapping("/showByNickname")
    public Result<List<PostVO>> showPostByNickname(@RequestParam String nickname){
        log.info("访问网名为{}的帖子",nickname);
        return Result.success(postInformationService.showByNickname(nickname));
    }

    /**
     * 根据信息，模糊搜索标题对应的帖子
     * @param title
     * @return
     */
    @GetMapping("/showByTitle")
    public  Result<List<PostVO>> showPostByTitle(@RequestParam String title){
        log.info("根据标题模糊搜索帖子");
        return Result.success(postInformationService.showByTitle(title));
    }




    /**
     * 根据帖子id修改帖子状态
     * @param postStatusDTO
     * @return
     */
    @PostMapping("/updateStatus")
    public Result<String> UpdatePostStatus(@RequestBody PostStatusDTO postStatusDTO){
        log.info("删除用户的帖子");
        postInformationService.updatePostStatus(postStatusDTO);
        return Result.success();
    }

    @PostMapping("/updatePost")
    public Result<String> updatePost(@RequestBody PostDTO postDTO){
        log.info("开始进行用户帖子修改");
        postInformationService.updatePost(postDTO);
        return Result.success();
    }


    /**
     * 预先将用户的头像数据传向后端处理成String格式
     */
    @PostMapping("/avatar")
    public Result<List<String>> handleAvatar(List<MultipartFile> avatars) throws IOException {
        log.info("预处理用户图片信息");
        if(avatars.getFirst()==null){
            log.info("图片无法解析或为空");
            return Result.error("图片无法解析或为空");
        }
        List<String> files = new ArrayList<>();
        for(MultipartFile avatar : avatars){
            String file = modifyUserAvatarPathFormatUtil.save(avatar);
            files.add(file);
        }
        return Result.success(files);
    }



}
