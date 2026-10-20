package com.ddbz.daymark.service;

import com.ddbz.daymark.pojo.dto.function.LikeDTO;
import com.ddbz.daymark.pojo.vo.function.LikeVO;

import java.util.List;

public interface LikeService {
    public void Like(int postId);

    public void DeleteLike(int postId);

    public List<LikeVO> LikeInformation(int postId);

    public Boolean IsLike(int postId);
}
