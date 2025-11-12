package com.anas.springblog.post.service;

import com.anas.springblog.authentecation.model.CustomUserDetails;
import com.anas.springblog.post.dto.PostRequest;
import com.anas.springblog.post.dto.PostResponse;
import com.anas.springblog.post.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public interface IPostService {
    @Transactional(readOnly = true)
    Page<PostResponse> getAllPosts(String tag,Pageable pageable);

    @Transactional(readOnly = true)
    Page<PostResponse> getPostByUser(CustomUserDetails userDetails, Pageable pageable);

    @Transactional(readOnly = true)
    PostResponse getPostWithId(Long id);

    @Transactional
    PostResponse createPost(PostRequest postRequest, CustomUserDetails userDetails);

    @Transactional
    PostResponse editPost(Long id, PostRequest postRequest, CustomUserDetails userDetails);

    Post findAndVerifyPostOwner(Long postId, CustomUserDetails userDetails);

    Post findPostById(Long postId);

    @Transactional
    void deletePost(Long id, CustomUserDetails userDetails) ;

    @Transactional(readOnly = true)
    Page<PostResponse> searchPosts(String keyword,Pageable pageable);

}
