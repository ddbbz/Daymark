package com.ddbz.daymark.service;

import com.ddbz.daymark.pojo.dto.function.CommentDTO;
import com.ddbz.daymark.pojo.vo.function.CommentVO;

import java.util.List;

public interface CommentService {

    public void Comment(CommentDTO commentDTO);

    public void DeleteComment(CommentDTO commentDTO);

    public List<CommentVO> GetComment(int postId);
}
