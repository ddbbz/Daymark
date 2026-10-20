package com.ddbz.daymark.controller.function;

import com.ddbz.daymark.common.Result;
import com.ddbz.daymark.context.BaseContext;
import com.ddbz.daymark.pojo.dto.function.LikeDTO;
import com.ddbz.daymark.pojo.vo.function.LikeVO;
import com.ddbz.daymark.service.LikeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("like")
public class LikeController {

    @Autowired
    private LikeService likeService;


    /**
     * 点赞
     * @param postId
     * @return
     */
    @PutMapping
    public Result<String> like(@RequestParam int postId){
        log.info("用户:{}的点赞操作开始", BaseContext.getCurrentUid());
        likeService.Like(postId);
        return Result.success();
    }


    /**
     * 取消点赞
     * @param postId
     * @return
     */
    @DeleteMapping
    public Result<String> deleteLike(@RequestParam int postId){
        log.info("取消点赞操作开始");
        likeService.DeleteLike(postId);
        return Result.success();
    }

    /**
     * 获取帖子所有点赞
     * @param postId
     * @return
     */
    @GetMapping("/likeInformation")
    public Result<List<LikeVO>> getLikeInformation(@RequestParam int postId){
        log.info("获取帖子所有点赞");
        return Result.success(likeService.LikeInformation(postId));
    }


    @GetMapping("isLike")
    public Result<Boolean> isLike(@RequestParam int postId){
        log.info("判断该帖子是否点赞");
        return Result.success(likeService.IsLike(postId));
    }
}
