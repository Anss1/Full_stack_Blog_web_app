package com.anas.springblog.comment.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentRequest(
        @NotBlank(message = "comment should not be empty")
        String text){}