package com.anas.springblog.comment.service;

import com.anas.springblog.authentecation.model.CustomUserDetails;
import com.anas.springblog.comment.dto.CommentRequest;
import com.anas.springblog.comment.dto.CommentResponse;
import com.anas.springblog.comment.model.Comment;
import com.anas.springblog.comment.repository.CommentRepository;
import com.anas.springblog.exception.ResourceNotFoundException;
import com.anas.springblog.post.model.Post;
import com.anas.springblog.post.service.IPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImp implements ICommentService {

    private final CommentRepository commentRepository;
    private final IPostService postService;

    @Transactional(readOnly = true)
    @Override
    public List<CommentResponse> getAllComments(Long postId) {
        return commentRepository.findAllByPostId(postId).stream()
                .map(c -> CommentResponse.fromEntity(c))
                .toList();
    }

    @Transactional
    @Override
    public CommentResponse createComment(Long postId, CommentRequest commentRequest, CustomUserDetails userDetails) {
        Post post = postService.findPostById(postId);
        Comment comment = Comment.builder()
                .text(commentRequest.text())
                .post(post)
                .user(userDetails.getUser())
                .build();
        return CommentResponse.fromEntity(_saveComment(comment));
    }

    @Transactional
    @Override
    public CommentResponse editComment(Long id, CommentRequest commentRequest, CustomUserDetails userDetails) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));
        if (!comment.getUser().getId().equals(userDetails.getUser().getId())){
            throw new AccessDeniedException("Not authorized");
        }
        comment.setText(commentRequest.text());
        return CommentResponse.fromEntity(_saveComment(comment));
    }

    @Transactional
    public void deleteComment(Long id, CustomUserDetails userDetails) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));
        if (!comment.getUser().getId().equals(userDetails.getUser().getId())){
            throw new AccessDeniedException("Not authorized");
        }
        commentRepository.delete(comment);
    }

    private Comment _saveComment(Comment comment){
        return commentRepository.save(comment);
    }
}
