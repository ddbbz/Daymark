package com.ddbz.daymark.service;


import com.ddbz.daymark.pojo.dto.post.PostDTO;
import com.ddbz.daymark.pojo.dto.post.PostStatusDTO;
import com.ddbz.daymark.pojo.vo.post.PostVO;

import java.io.IOException;
import java.util.List;

public interface PostInformationService {
    public void publishPost(PostDTO postDTO) throws IOException;

    public List<PostVO> showByUid(String uid);

    public List<PostVO> showByNickname(String nickname);

    public List<PostVO> showByTitle(String title);

    public void updatePostStatus(PostStatusDTO postStatusDTO);

    public void updatePost(PostDTO postDTO);

}
