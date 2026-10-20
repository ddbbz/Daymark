package com.ddbz.daymark.controller.reviewer;

import com.ddbz.daymark.common.Result;
import com.ddbz.daymark.pojo.dto.post.PostDTO;
import com.ddbz.daymark.pojo.dto.post.PostStatusDTO;
import com.ddbz.daymark.pojo.vo.post.PostVO;
import com.ddbz.daymark.service.ReviewerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("review")
public class ReviewController {

    @Autowired
    private ReviewerService reviewerService;

    /**
     * 获得需要审核的帖子
     * @return
     */
    @GetMapping
    public Result<List<PostVO>> getReviewingPost(){
        log.info("开始申请获取需要审核的帖子");
        return Result.success(reviewerService.GetReviewingPost());
    }


    @PostMapping
    public Result<Object> updatePostStatus(@RequestBody PostStatusDTO postStatusDTO){
        log.info("开始修改要审核的帖子的状态");
        reviewerService.UpdatePostStatus(postStatusDTO);
        return Result.success();
    }
}
