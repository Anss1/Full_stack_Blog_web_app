package com.anas.springblog.post.dto;

import com.anas.springblog.post.model.Post;
import com.anas.springblog.tag.model.Tag;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record PostResponse (
     Long id,
     String title,
     String content,
     String authorName,
     LocalDateTime createdAt,
     Set<String> tags
){
    public static PostResponse fromEntity(Post post){
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getUser().getUsername(),
                post.getCreatedAt(),
                post.getTags().stream().map(Tag::getName).collect(Collectors.toSet())
        );
    }
}
