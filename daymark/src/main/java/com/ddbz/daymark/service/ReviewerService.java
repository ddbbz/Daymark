package com.ddbz.daymark.service;

import com.ddbz.daymark.pojo.dto.post.PostDTO;
import com.ddbz.daymark.pojo.dto.post.PostStatusDTO;
import com.ddbz.daymark.pojo.dto.reviewer.ReviewerDTO;
import com.ddbz.daymark.pojo.vo.LoginVO;
import com.ddbz.daymark.pojo.vo.post.PostVO;

import java.util.List;

public interface ReviewerService {

    public void Register(ReviewerDTO reviewerDTO);

    public LoginVO Login(ReviewerDTO reviewerDTO);

    public List<PostVO> GetReviewingPost();

    public void UpdatePostStatus(PostStatusDTO postStatusDTO);
}
