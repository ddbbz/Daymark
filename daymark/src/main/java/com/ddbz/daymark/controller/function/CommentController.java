package com.ddbz.daymark.controller.function;

import com.ddbz.daymark.common.Result;
import com.ddbz.daymark.context.BaseContext;
import com.ddbz.daymark.pojo.dto.function.CommentDTO;
import com.ddbz.daymark.pojo.dto.post.PostDTO;
import com.ddbz.daymark.pojo.vo.function.CommentVO;
import com.ddbz.daymark.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("comment")
@Slf4j
@RestController
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 评论操作
     */
    @PutMapping
    public Result<String> comment(@RequestBody CommentDTO commentDTO){
        log.info("用户{}开始进行评论操作", BaseContext.getCurrentUid());
        commentService.Comment(commentDTO);
        return Result.success("用户评论操作成功");
    }

    /**
     * 删除评论
     */
    @DeleteMapping
    public Result<String> deleteComment(@RequestBody CommentDTO commentDTO){
        log.info("删除评论操作");
        commentService.DeleteComment(commentDTO);
        return Result.success("用户评论删除成功");
    }

    /**
     * 获取评论
     */
    @GetMapping
    public Result<List<CommentVO>> getComment(@RequestParam int postId){
        log.info("获取评论信息");
        List<CommentVO> comments = commentService.GetComment(postId);
        return Result.success(comments);
    }
}
