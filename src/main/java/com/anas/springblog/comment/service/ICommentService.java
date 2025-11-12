package com.anas.springblog.comment.service;

import com.anas.springblog.authentecation.model.CustomUserDetails;
import com.anas.springblog.comment.dto.CommentRequest;
import com.anas.springblog.comment.dto.CommentResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ICommentService {
    @Transactional(readOnly = true)
    List<CommentResponse> getAllComments(Long postId);

    @Transactional
    CommentResponse createComment(Long postId, CommentRequest commentRequest, CustomUserDetails userDetails);

    @Transactional
    CommentResponse editComment(Long id, CommentRequest commentRequest, CustomUserDetails userDetails);

    @Transactional
    void deleteComment(Long id, CustomUserDetails user);
}
