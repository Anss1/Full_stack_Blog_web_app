package com.anas.springblog.comment.dto;

import com.anas.springblog.comment.model.Comment;

public record CommentResponse (
     Long id,
     String text,
     String authorName
){
    public static CommentResponse fromEntity(Comment comment){
        return new CommentResponse(
                comment.getId(),
                comment.getText(),
                comment.getUser().getUsername()
        );
    }
}
