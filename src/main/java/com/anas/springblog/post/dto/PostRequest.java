package com.anas.springblog.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record PostRequest(
        @NotBlank(message = "title can not be emtpy")
        @Size(min = 1, max = 50, message = "title should be at least 1 to 50 character")
        String title,
        @NotBlank(message = "content can not be emtpy")
        @Size(min = 1, message = "content should be at least 1 character")
        String content,
        Set<String> tags
) {}
