package com.ddbz.daymark.controller.reviewer;

import com.ddbz.daymark.common.Result;
import com.ddbz.daymark.pojo.dto.reviewer.ReviewerDTO;
import com.ddbz.daymark.pojo.vo.LoginVO;
import com.ddbz.daymark.service.ReviewerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("reviewerLogin")
@RestController
public class ReviewerLoginController {

    @Autowired
    private ReviewerService reviewerService;

    @PostMapping
    public Result<LoginVO> login(@RequestBody ReviewerDTO reviewerDTO) {
        log.info("审查员登录操作开始");
        return Result.success(reviewerService.Login(reviewerDTO));

    }

}
