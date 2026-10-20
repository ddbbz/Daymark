package com.ddbz.daymark.controller.reviewer;

import com.ddbz.daymark.common.Result;
import com.ddbz.daymark.pojo.dto.reviewer.ReviewerDTO;
import com.ddbz.daymark.service.ReviewerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("reviewerRegister")
@RestController
public class ReviewerRegisterController {

    @Autowired
    private ReviewerService reviewerService;

    /**
     * 注册操作
     * @param reviewerDTO
     * @return
     */
    @PostMapping
    public Result<Object> register(@RequestBody ReviewerDTO reviewerDTO){
        log.info("用户{}申请进行注册操作",reviewerDTO.getUid());
        reviewerService.Register(reviewerDTO);
        return Result.success();
    }


}
