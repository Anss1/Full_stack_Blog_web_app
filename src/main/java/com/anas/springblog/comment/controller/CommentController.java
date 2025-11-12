package com.anas.springblog.comment.controller;

import com.anas.springblog.authentecation.model.CustomUserDetails;
import com.anas.springblog.comment.dto.CommentRequest;
import com.anas.springblog.comment.dto.CommentResponse;
import com.anas.springblog.comment.service.CommentServiceImp;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CommentController {

    private final CommentServiceImp commentService;

    @GetMapping("/posts/{postId}/comments")
    public List<CommentResponse> getAllComments(@PathVariable Long postId) {
        return commentService.getAllComments(postId);
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(@PathVariable Long postId, @Valid @RequestBody CommentRequest commentRequest
                                                            , @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.createComment(postId,commentRequest,userDetails));
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponse> editComment(@PathVariable Long commentId
                                        , @Valid @RequestBody CommentRequest commentRequest
                                        , @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.OK).body(commentService.editComment(commentId,commentRequest,userDetails));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId
            , @AuthenticationPrincipal CustomUserDetails userDetails) {
        commentService.deleteComment(commentId,userDetails);
        return ResponseEntity.noContent().build();
    }

}
